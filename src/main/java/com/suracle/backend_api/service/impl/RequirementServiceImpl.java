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
            if (aiResult.containsKey("llm_summary")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> llmSummary = (Map<String, Object>) aiResult.get("llm_summary");
                if (llmSummary != null && llmSummary.containsKey("confidence_score")) {
                    confidenceScore = ((Number) llmSummary.get("confidence_score")).doubleValue();
                }
            }
            
            StringBuilder analysisSummary = new StringBuilder();
            if (isValid && aiResult.containsKey("recommended_agencies")) {
                @SuppressWarnings("unchecked")
                var agencies = (Iterable<?>) aiResult.get("recommended_agencies");
                if (agencies != null) {
                    for (Object agency : agencies) {
                        analysisSummary.append(agency.toString()).append(" ");
                    }
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
                    .isValid(isValid)
                    .confidenceScore(confidenceScore)
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