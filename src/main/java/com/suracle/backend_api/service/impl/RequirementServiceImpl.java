package com.suracle.backend_api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suracle.backend_api.dto.requirement.RequirementAnalysisResponse;
import com.suracle.backend_api.entity.product.Product;
import com.suracle.backend_api.repository.ProductRepository;
import com.suracle.backend_api.service.AiWorkflowService;
import com.suracle.backend_api.service.RequirementService;
import com.suracle.backend_api.service.http.RequirementsApiClient;
import com.suracle.backend_api.service.util.EnglishNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequirementServiceImpl implements RequirementService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RequirementsApiClient apiClient;
    private final AiWorkflowService aiWorkflowService;
    private final ProductRepository productRepository;

    /**
     * AI 엔진을 통한 요구사항 분석 실행
     * @param productId 분석할 상품 ID
     * @return 요구사항 분석 결과
     */
    @Override
    public RequirementAnalysisResponse getRequirementAnalysis(Long productId) {
        try {
            log.info("AI 엔진을 통한 요구사항 분석 시작 - productId: {}", productId);
            
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
            
            // AI 엔진을 통한 요구사항 분석 실행
            Map<String, Object> aiResult = aiWorkflowService.executeRequirementsAnalysis(product);
            
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
            RequirementAnalysisResponse response = convertAiResultToResponse(productId, product.getProductName(), aiResult);
            
            log.info("AI 엔진 요구사항 분석 완료 - productId: {}, 신뢰도: {}, 유효성: {}", 
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
            // AI 엔진 응답에서 데이터 추출
            boolean isValid = !aiResult.containsKey("error") && "completed".equals(aiResult.get("status"));
            
            Double confidenceScore = 0.85; // 기본값
            String hsCode = (String) aiResult.get("hs_code");
            
            // llm_summary에서 상세 데이터 추출
            List<String> criticalActions = new ArrayList<>();
            List<String> requiredDocuments = new ArrayList<>();
            List<String> complianceSteps = new ArrayList<>();
            String timeline = "";
            
            if (aiResult.containsKey("llm_summary")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> llmSummary = (Map<String, Object>) aiResult.get("llm_summary");
                if (llmSummary != null) {
                    // 신뢰도 점수
                    if (llmSummary.containsKey("confidence_score")) {
                        confidenceScore = ((Number) llmSummary.get("confidence_score")).doubleValue();
                    }
                    
                    // 타임라인
                    if (llmSummary.containsKey("timeline")) {
                        timeline = (String) llmSummary.get("timeline");
                    }
                    
                    // 핵심 요구사항 (critical_requirements를 criticalActions로 매핑)
                    if (llmSummary.containsKey("critical_requirements")) {
                        @SuppressWarnings("unchecked")
                        List<String> criticalReqs = (List<String>) llmSummary.get("critical_requirements");
                        if (criticalReqs != null) {
                            criticalActions.addAll(criticalReqs);
                        }
                    }
                    
                    // 필수 문서 목록
                    if (llmSummary.containsKey("required_documents")) {
                        @SuppressWarnings("unchecked")
                        List<String> docs = (List<String>) llmSummary.get("required_documents");
                        if (docs != null) {
                            requiredDocuments.addAll(docs);
                        }
                    }
                    
                    // 준수 단계
                    if (llmSummary.containsKey("compliance_steps")) {
                        @SuppressWarnings("unchecked")
                        List<String> steps = (List<String>) llmSummary.get("compliance_steps");
                        if (steps != null) {
                            complianceSteps.addAll(steps);
                        }
                    }
                }
            }
            
            // 추천 기관을 sources로 변환
            List<String> sources = new ArrayList<>();
            if (aiResult.containsKey("recommended_agencies")) {
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
            
            return RequirementAnalysisResponse.builder()
                    .productId(String.valueOf(productId))
                    .productName(productName)
                    .hsCode(hsCode != null ? hsCode : "")
                    .criticalActions(criticalActions)
                    .requiredDocuments(requiredDocuments)
                    .complianceSteps(complianceSteps)
                    .timeline(timeline)
                    .sources(sources)
                    .confidenceScore(confidenceScore)
                    .isValid(isValid)
                    .pendingAnalysis(isValid ? "AI 분석 완료" : analysisSummary.toString())
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
}