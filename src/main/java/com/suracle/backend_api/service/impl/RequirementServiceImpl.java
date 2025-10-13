package com.suracle.backend_api.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suracle.backend_api.dto.requirement.RequirementAnalysisResponse;
import com.suracle.backend_api.entity.cache.ProductAnalysisCache;
import com.suracle.backend_api.entity.product.Product;
import com.suracle.backend_api.repository.ProductAnalysisCacheRepository;
import com.suracle.backend_api.repository.ProductRepository;
import com.suracle.backend_api.service.AiWorkflowService;
import com.suracle.backend_api.service.RequirementService;
import com.suracle.backend_api.service.http.RequirementsApiClient;
import com.suracle.backend_api.service.util.EnglishNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequirementServiceImpl implements RequirementService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RequirementsApiClient apiClient;
    private final AiWorkflowService aiWorkflowService;
    private final ProductRepository productRepository;
    private final ProductAnalysisCacheRepository productAnalysisCacheRepository;
    
    // JSON 파일 저장 경로
    private static final String REQUIREMENTS_DIR = "requirements_results";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    /**
     * AI 엔진을 통한 요구사항 분석 실행
     * @param productId 분석할 상품 ID
     * @return 요구사항 분석 결과
     */
    @Override
    public RequirementAnalysisResponse getRequirementAnalysis(Long productId) {
        try {
            log.info("요구사항 분석 조회 시작 - productId: {}", productId);
            
            // 상품 정보 조회
            Product product = productRepository.findById(productId.intValue())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + productId));
            
            if (product.getHsCode() == null || product.getHsCode().trim().isEmpty()) {
                log.warn("HS코드가 없는 상품 - productId: {}, 상품명: {}", productId, product.getProductName());
                return RequirementAnalysisResponse.builder()
                        .productId(String.valueOf(productId))
                        .productName(product.getProductName())
                        .isValid(false)
                        .confidenceScore(0.0)
                        .pendingAnalysis("HS코드가 없어 분석할 수 없습니다")
                        .build();
            }
            
            // 1. DB 캐시에서 먼저 조회
            Optional<ProductAnalysisCache> cachedAnalysis = productAnalysisCacheRepository
                    .findByProductIdAndAnalysisType(product.getId(), "requirements");
            
            Map<String, Object> aiResult;
            
            if (cachedAnalysis.isPresent()) {
                log.info("✅ DB 캐시에서 요건 분석 결과 조회 - productId: {}", productId);
                ProductAnalysisCache cache = cachedAnalysis.get();
                aiResult = objectMapper.convertValue(cache.getAnalysisResult(), Map.class);
            } else {
                log.info("🤖 DB 캐시 없음, AI 엔진 호출 - productId: {}", productId);
                // 2. 캐시 없으면 AI 엔진 호출
                aiResult = aiWorkflowService.executeRequirementsAnalysis(product);
            }
            
            if (aiResult == null || aiResult.isEmpty()) {
                log.error("AI 엔진 분석 결과가 비어있음 - productId: {}", productId);
                return RequirementAnalysisResponse.builder()
                        .productId(String.valueOf(productId))
                        .productName(product.getProductName())
                        .isValid(false)
                        .confidenceScore(0.0)
                        .pendingAnalysis("AI 엔진 분석 결과가 비어있습니다")
                        .build();
            }
            
            // AI 엔진 응답을 RequirementAnalysisResponse로 변환
            RequirementAnalysisResponse response;
            try {
                response = convertAiResultToResponse(productId, product.getProductName(), aiResult);
            } catch (Exception e) {
                log.error("❌ AI 결과 변환 실패 (계속 진행) - productId: {}, 오류: {}", productId, e.getMessage());
                // 변환 실패해도 기본 응답 생성
                response = RequirementAnalysisResponse.builder()
                        .productId(String.valueOf(productId))
                        .productName(product.getProductName())
                        .isValid(false)
                        .confidenceScore(0.5)
                        .pendingAnalysis("분석 결과 변환 중 오류 발생")
                        .build();
            }
            
            // JSON 파일로 저장 (AI 엔진 호출 시에만, DB 캐시가 없었을 때만)
            // 에러가 나도 반드시 저장 시도
            if (cachedAnalysis.isEmpty()) {
                try {
                    saveRequirementResultToJson(product.getProductName(), aiResult);
                    log.info("💾 JSON 파일 저장 성공 - productId: {}", productId);
                } catch (Exception e) {
                    log.error("❌ JSON 파일 저장 실패 (계속 진행) - productId: {}, 오류: {}", productId, e.getMessage());
                    // JSON 저장 실패해도 계속 진행
                }
            }
            
            log.info("✅ AI 엔진 요구사항 분석 완료 - productId: {}, 신뢰도: {}, 유효성: {}", 
                    productId, response.getConfidenceScore(), response.isValid());
            
            return response;
            
        } catch (Exception e) {
            log.error("요구사항 분석 실행 실패 - productId: {}, 오류: {}", productId, e.getMessage(), e);
            return RequirementAnalysisResponse.builder()
                    .productId(String.valueOf(productId))
                    .isValid(false)
                    .confidenceScore(0.0)
                    .pendingAnalysis("요구사항 분석 중 오류 발생: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * AI 엔진 응답을 RequirementAnalysisResponse로 변환
     */
    private RequirementAnalysisResponse convertAiResultToResponse(Long productId, String productName, Map<String, Object> aiResult) {
        try {
            // DB 캐시에서 온 데이터인지 확인 (필드 구조가 다름)
            boolean isFromCache = aiResult.containsKey("critical_actions");
            
            // AI 엔진 응답에서 데이터 추출
            boolean isValid = !aiResult.containsKey("error") && 
                            (isFromCache || "completed".equals(aiResult.get("status")));
            
            Double confidenceScore = 0.85; // 기본값
            String hsCode = aiResult.get("hs_code") != null ? aiResult.get("hs_code").toString() : "";
            
            // 데이터 추출 (DB 캐시 vs AI 엔진 응답)
            List<Object> criticalActions = new ArrayList<>();
            List<Object> requiredDocuments = new ArrayList<>();
            List<Object> complianceSteps = new ArrayList<>();
            Object timeline = "";
            
            if (isFromCache) {
                // DB 캐시 데이터 (data.sql 형식)
                log.info("📦 DB 캐시 데이터 파싱");
                
                if (aiResult.containsKey("critical_actions")) {
                    @SuppressWarnings("unchecked")
                    List<Object> actions = (List<Object>) aiResult.get("critical_actions");
                    if (actions != null) criticalActions.addAll(actions);
                }
                
                if (aiResult.containsKey("required_documents")) {
                    @SuppressWarnings("unchecked")
                    List<Object> docs = (List<Object>) aiResult.get("required_documents");
                    if (docs != null) requiredDocuments.addAll(docs);
                }
                
                if (aiResult.containsKey("compliance_steps")) {
                    @SuppressWarnings("unchecked")
                    List<Object> steps = (List<Object>) aiResult.get("compliance_steps");
                    if (steps != null) complianceSteps.addAll(steps);
                }
                
                timeline = aiResult.getOrDefault("timeline", "");
                
            } else if (aiResult.containsKey("llm_summary")) {
                // AI 엔진 응답 (llm_summary 형식)
                log.info("🤖 AI 엔진 응답 데이터 파싱");
                
                @SuppressWarnings("unchecked")
                Map<String, Object> llmSummary = (Map<String, Object>) aiResult.get("llm_summary");
                if (llmSummary != null) {
                    // 신뢰도 점수
                    if (llmSummary.containsKey("confidence_score")) {
                        confidenceScore = ((Number) llmSummary.get("confidence_score")).doubleValue();
                    }
                    
                    // 타임라인
                    if (llmSummary.containsKey("timeline")) {
                        timeline = llmSummary.get("timeline");
                    }
                    
                    // critical_requirements
                    if (llmSummary.containsKey("critical_requirements")) {
                        @SuppressWarnings("unchecked")
                        List<Object> reqs = (List<Object>) llmSummary.get("critical_requirements");
                        if (reqs != null) criticalActions.addAll(reqs);
                    }
                    
                    // required_documents
                    if (llmSummary.containsKey("required_documents")) {
                        @SuppressWarnings("unchecked")
                        List<Object> docs = (List<Object>) llmSummary.get("required_documents");
                        if (docs != null) requiredDocuments.addAll(docs);
                    }
                    
                    // compliance_steps
                    if (llmSummary.containsKey("compliance_steps")) {
                        @SuppressWarnings("unchecked")
                        List<Object> steps = (List<Object>) llmSummary.get("compliance_steps");
                        if (steps != null) complianceSteps.addAll(steps);
                    }
                }
            }
            
            // sources 추출 (각 항목의 source_url에서)
            List<Object> sources = new ArrayList<>();
            Set<String> uniqueSources = new HashSet<>();
            
            // critical_actions에서 source_url 추출
            for (Object action : criticalActions) {
                if (action instanceof Map) {
                    Map<?, ?> actionMap = (Map<?, ?>) action;
                    Object sourceUrl = actionMap.get("source_url");
                    if (sourceUrl != null && !uniqueSources.contains(sourceUrl.toString())) {
                        sources.add(sourceUrl.toString());
                        uniqueSources.add(sourceUrl.toString());
                    }
                }
            }
            
            // required_documents에서 source_url 추출
            for (Object doc : requiredDocuments) {
                if (doc instanceof Map) {
                    Map<?, ?> docMap = (Map<?, ?>) doc;
                    Object sourceUrl = docMap.get("source_url");
                    if (sourceUrl != null && !uniqueSources.contains(sourceUrl.toString())) {
                        sources.add(sourceUrl.toString());
                        uniqueSources.add(sourceUrl.toString());
                    }
                }
            }
            
            // compliance_steps에서 source_url 추출
            for (Object step : complianceSteps) {
                if (step instanceof Map) {
                    Map<?, ?> stepMap = (Map<?, ?>) step;
                    Object sourceUrl = stepMap.get("source_url");
                    if (sourceUrl != null && !uniqueSources.contains(sourceUrl.toString())) {
                        sources.add(sourceUrl.toString());
                        uniqueSources.add(sourceUrl.toString());
                    }
                }
            }
            
            // timeline에서 source_url 추출
            if (timeline instanceof Map) {
                Map<?, ?> timelineMap = (Map<?, ?>) timeline;
                Object sourceUrl = timelineMap.get("source_url");
                if (sourceUrl != null && !uniqueSources.contains(sourceUrl.toString())) {
                    sources.add(sourceUrl.toString());
                    uniqueSources.add(sourceUrl.toString());
                }
            }
            
            // 추천 기관을 sources로 변환 (fallback)
            if (sources.isEmpty() && aiResult.containsKey("recommended_agencies")) {
                @SuppressWarnings("unchecked")
                List<String> agencies = (List<String>) aiResult.get("recommended_agencies");
                if (agencies != null) {
                    for (String agency : agencies) {
                        switch (agency.toUpperCase()) {
                            case "FDA":
                                sources.add("https://www.fda.gov/cosmetics/cosmetics-laws-regulations");
                                break;
                            case "EPA":
                                sources.add("https://www.epa.gov/laws-regulations");
                                break;
                            case "USDA":
                                sources.add("https://www.usda.gov/topics");
                                break;
                            case "CPSC":
                                sources.add("https://www.cpsc.gov/Regulations-Laws--Standards");
                                break;
                            case "FCC":
                                sources.add("https://www.fcc.gov/engineering-technology/rules-regulations");
                                break;
                            case "CBP":
                                sources.add("https://www.cbp.gov/trade/programs-administration");
                                break;
                        }
                    }
                }
            }
            
            // 분석 요약 생성
            StringBuilder analysisSummary = new StringBuilder();
            if (isValid && aiResult.containsKey("recommended_agencies")) {
                @SuppressWarnings("unchecked")
                List<String> agencies = (List<String>) aiResult.get("recommended_agencies");
                if (agencies != null && !agencies.isEmpty()) {
                    analysisSummary.append("추천 기관: ").append(String.join(", ", agencies));
                }
            }
            
            if (!isValid && aiResult.containsKey("error")) {
                analysisSummary.append("AI 엔진 연결 실패");
            } else if (!isValid) {
                analysisSummary.append("분석 실패");
            }
            
            // Object를 그대로 유지 (프론트엔드가 객체/문자열 둘 다 처리 가능)
            List<Object> criticalActionsStr = new ArrayList<>(criticalActions);
            List<Object> requiredDocumentsStr = new ArrayList<>(requiredDocuments);
            List<Object> complianceStepsStr = new ArrayList<>(complianceSteps);
            
            log.info("✅ 데이터 변환 완료 - actions: {}, docs: {}, steps: {}",
                    criticalActionsStr.size(), requiredDocumentsStr.size(), complianceStepsStr.size());
            
            // Phase 2-4 전문 분석 결과 추출
            Object detailedRegulations = null;
            Object testingProcedures = null;
            Object penalties = null;
            Object validity = null;
            Object crossValidation = null;
            
            if (aiResult.containsKey("detailed_regulations")) {
                detailedRegulations = aiResult.get("detailed_regulations");
                log.debug("📋 세부 규정 데이터 추출");
            }
            if (aiResult.containsKey("testing_procedures")) {
                testingProcedures = aiResult.get("testing_procedures");
                log.debug("🧪 검사 절차 데이터 추출");
            }
            if (aiResult.containsKey("penalties")) {
                penalties = aiResult.get("penalties");
                log.debug("⚖️ 처벌 데이터 추출");
            }
            if (aiResult.containsKey("validity")) {
                validity = aiResult.get("validity");
                log.debug("⏰ 유효기간 데이터 추출");
            }
            if (aiResult.containsKey("cross_validation")) {
                crossValidation = aiResult.get("cross_validation");
                log.debug("🔍 교차 검증 데이터 추출");
            }
            
            // 판례 검증 및 통합 신뢰도 추출 (신규 - 2025-10-12)
            Object precedentValidation = null;
            Object overallConfidence = null;
            Object verificationSummary = null;
            if (aiResult.containsKey("precedent_validation")) {
                precedentValidation = aiResult.get("precedent_validation");
                log.debug("📜 판례 검증 데이터 추출");
            }
            if (aiResult.containsKey("overall_confidence")) {
                overallConfidence = aiResult.get("overall_confidence");
                log.debug("🎯 통합 신뢰도 데이터 추출");
            }
            if (aiResult.containsKey("verification_summary")) {
                verificationSummary = aiResult.get("verification_summary");
                log.debug("✅ 검증 요약 데이터 추출");
            }
            
            // 확장 필드 추출 (신규 - 2025-10-12)
            Object executionChecklist = null;
            Object costBreakdown = null;
            Object riskMatrix = null;
            Object complianceScore = null;
            Object marketAccess = null;
            Object estimatedCosts = null;
            List<Object> riskFactors = new ArrayList<>();
            List<Object> recommendations = new ArrayList<>();
            Object timelineDetail = null;
            List<Object> labelingRequirements = new ArrayList<>();
            List<Object> testingRequirementsList = new ArrayList<>();
            List<Object> prohibitedSubstances = new ArrayList<>();
            List<Object> priorNotifications = new ArrayList<>();
            List<Object> exemptions = new ArrayList<>();
            
            // llm_summary에서 확장 필드 추출
            @SuppressWarnings("unchecked")
            Map<String, Object> llmSummaryForExtended = (Map<String, Object>) aiResult.get("llm_summary");
            if (llmSummaryForExtended != null) {
                executionChecklist = llmSummaryForExtended.get("execution_checklist");
                costBreakdown = llmSummaryForExtended.get("cost_breakdown");
                riskMatrix = llmSummaryForExtended.get("risk_matrix");
                complianceScore = llmSummaryForExtended.get("compliance_score");
                marketAccess = llmSummaryForExtended.get("market_access");
                estimatedCosts = llmSummaryForExtended.get("estimated_costs");
                timelineDetail = llmSummaryForExtended.get("timeline");
                
                // 리스트 필드
                if (llmSummaryForExtended.containsKey("risk_factors")) {
                    @SuppressWarnings("unchecked")
                    List<Object> risks = (List<Object>) llmSummaryForExtended.get("risk_factors");
                    if (risks != null) riskFactors.addAll(risks);
                }
                if (llmSummaryForExtended.containsKey("recommendations")) {
                    @SuppressWarnings("unchecked")
                    List<Object> recs = (List<Object>) llmSummaryForExtended.get("recommendations");
                    if (recs != null) recommendations.addAll(recs);
                }
                if (llmSummaryForExtended.containsKey("labeling_requirements")) {
                    @SuppressWarnings("unchecked")
                    List<Object> labeling = (List<Object>) llmSummaryForExtended.get("labeling_requirements");
                    if (labeling != null) labelingRequirements.addAll(labeling);
                }
                if (llmSummaryForExtended.containsKey("testing_requirements")) {
                    @SuppressWarnings("unchecked")
                    List<Object> testing = (List<Object>) llmSummaryForExtended.get("testing_requirements");
                    if (testing != null) testingRequirementsList.addAll(testing);
                }
                if (llmSummaryForExtended.containsKey("prohibited_restricted_substances")) {
                    @SuppressWarnings("unchecked")
                    List<Object> substances = (List<Object>) llmSummaryForExtended.get("prohibited_restricted_substances");
                    if (substances != null) prohibitedSubstances.addAll(substances);
                }
                if (llmSummaryForExtended.containsKey("prior_notifications")) {
                    @SuppressWarnings("unchecked")
                    List<Object> notifications = (List<Object>) llmSummaryForExtended.get("prior_notifications");
                    if (notifications != null) priorNotifications.addAll(notifications);
                }
                if (llmSummaryForExtended.containsKey("exemptions")) {
                    @SuppressWarnings("unchecked")
                    List<Object> exemps = (List<Object>) llmSummaryForExtended.get("exemptions");
                    if (exemps != null) exemptions.addAll(exemps);
                }
            }
            
            return RequirementAnalysisResponse.builder()
                    .productId(String.valueOf(productId))
                    .productName(productName)
                    .hsCode(hsCode != null ? hsCode : "")
                    .criticalActions(criticalActionsStr)
                    .requiredDocuments(requiredDocumentsStr)
                    .complianceSteps(complianceStepsStr)
                    .timeline(timeline)
                    .sources(sources)
                    .confidenceScore(confidenceScore)
                    .isValid(isValid)
                    .pendingAnalysis(isValid ? "AI 분석 완료" : analysisSummary.toString())
                    // Phase 2-4 전문 분석 결과 추가
                    .detailedRegulations(detailedRegulations)
                    .testingProcedures(testingProcedures)
                    .penalties(penalties)
                    .validity(validity)
                    .crossValidation(crossValidation)
                    // 판례 검증 및 통합 신뢰도 추가 (신규 - 2025-10-12)
                    .precedentValidation(precedentValidation)
                    .overallConfidence(overallConfidence)
                    .verificationSummary(verificationSummary)
                    // 확장 필드 추가 (신규 - 2025-10-12)
                    .executionChecklist(executionChecklist)
                    .costBreakdown(costBreakdown)
                    .riskMatrix(riskMatrix)
                    .complianceScore(complianceScore)
                    .marketAccess(marketAccess)
                    .estimatedCosts(estimatedCosts)
                    .riskFactors(riskFactors.isEmpty() ? null : riskFactors)
                    .recommendations(recommendations.isEmpty() ? null : recommendations)
                    .timelineDetail(timelineDetail)
                    .labelingRequirements(labelingRequirements.isEmpty() ? null : labelingRequirements)
                    .testingRequirements(testingRequirementsList.isEmpty() ? null : testingRequirementsList)
                    .prohibitedRestrictedSubstances(prohibitedSubstances.isEmpty() ? null : prohibitedSubstances)
                    .priorNotifications(priorNotifications.isEmpty() ? null : priorNotifications)
                    .exemptions(exemptions.isEmpty() ? null : exemptions)
                    .build();
                    
        } catch (Exception e) {
            log.error("AI 결과 변환 실패 - productId: {}, 오류: {}", productId, e.getMessage());
            return RequirementAnalysisResponse.builder()
                    .productId(String.valueOf(productId))
                    .productName(productName)
                    .isValid(false)
                    .confidenceScore(0.0)
                    .pendingAnalysis("결과 변환 중 오류 발생: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 요구사항 분석 결과를 JSON 파일로 저장
     * 파일명 형식: requirement_{상품명}_{YYMMDD}.json
     * 
     * @param productName 상품명
     * @param aiResult AI 엔진 분석 결과
     */
    private void saveRequirementResultToJson(String productName, Map<String, Object> aiResult) {
        try {
            // 디렉토리 생성
            Path dirPath = Paths.get(REQUIREMENTS_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                log.info("📁 요구사항 결과 저장 디렉토리 생성: {}", dirPath.toAbsolutePath());
            }
            
            // 파일명 생성: requirement_{상품명}_{YYMMDD}.json
            String sanitizedProductName = sanitizeFileName(productName);
            String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
            String fileName = String.format("requirement_%s_%s.json", sanitizedProductName, dateStr);
            Path filePath = dirPath.resolve(fileName);
            
            // JSON 파일로 저장 (예쁘게 포맷팅)
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), aiResult);
            
            log.info("💾 요구사항 분석 결과 JSON 저장 완료: {}", filePath.toAbsolutePath());
            
        } catch (IOException e) {
            log.error("❌ JSON 파일 저장 실패 - 상품명: {}, 오류: {}", productName, e.getMessage(), e);
        }
    }
    
    /**
     * 파일명에 사용할 수 없는 문자 제거 및 정리
     * 
     * @param name 원본 이름
     * @return 정리된 파일명
     */
    private String sanitizeFileName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "unknown";
        }
        
        // 파일명에 사용할 수 없는 문자 제거: \ / : * ? " < > |
        String sanitized = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        
        // 공백을 언더스코어로 변경
        sanitized = sanitized.replaceAll("\\s+", "_");
        
        // 연속된 언더스코어를 하나로
        sanitized = sanitized.replaceAll("_+", "_");
        
        // 앞뒤 언더스코어 제거
        sanitized = sanitized.replaceAll("^_+|_+$", "");
        
        // 최대 길이 제한 (50자)
        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 50);
        }
        
        // 빈 문자열이면 기본값
        if (sanitized.isEmpty()) {
            sanitized = "unknown";
        }
        
        return sanitized;
    }
}