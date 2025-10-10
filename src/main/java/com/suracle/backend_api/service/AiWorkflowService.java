package com.suracle.backend_api.service;

import com.suracle.backend_api.entity.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiWorkflowService {

    private final RestTemplate restTemplate;
    
    @Value("${ai.workflow.url:http://localhost:8000}")
    private String aiWorkflowUrl;
    
    @Value("${ai.precedents-analysis.url:http://localhost:8000}")
    private String precedentsAnalysisUrl;
    
    @Value("${ai.requirements-analysis.url:http://localhost:8000}")
    private String requirementsAnalysisUrl;

    /**
     * AI 워크플로우 실행 (전체 분석)
     */
    public Map<String, Object> executeAiWorkflow(Product product) {
        try {
            log.info("AI 워크플로우 실행 시작 - 상품 ID: {}, 상품명: {}", product.getProductId(), product.getProductName());
            
            // 요청 데이터 구성
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("product_name", product.getProductName());
            requestData.put("description", product.getDescription());
            requestData.put("hs_code", product.getHsCode());
            requestData.put("origin_country", product.getOriginCountry());
            requestData.put("price", product.getPrice());
            requestData.put("fob_price", product.getFobPrice());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);
            
            // AI 워크플로우 호출 (현재는 precedents-analysis만 호출)
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                precedentsAnalysisUrl + "/analyze-precedents",
                entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                if (result != null) {
                    log.info("AI 워크플로우 실행 완료 - 상품 ID: {}, 신뢰도: {}", 
                            product.getProductId(), result.get("confidence_score"));
                    return result;
                }
            }
            log.error("AI 워크플로우 실행 실패 - 상품 ID: {}, 상태코드: {}", 
                    product.getProductId(), response.getStatusCode());
            return getDefaultAnalysisResult();
            
        } catch (Exception e) {
            log.error("AI 워크플로우 실행 중 오류 발생 - 상품 ID: {}, 오류: {}", 
                     product.getProductId(), e.getMessage(), e);
            return getDefaultAnalysisResult();
        }
    }

    /**
     * 판례 분석만 실행
     */
    public Map<String, Object> executePrecedentsAnalysis(Product product) {
        try {
            log.info("판례 분석 실행 시작 - 상품 ID: {}, HS코드: {}", product.getProductId(), product.getHsCode());
            
            // 요청 데이터 구성
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("product_id", product.getProductId());
            requestData.put("product_name", product.getProductName());
            requestData.put("description", product.getDescription());
            requestData.put("hs_code", product.getHsCode());
            requestData.put("origin_country", product.getOriginCountry());
            requestData.put("price", product.getPrice() != null ? product.getPrice() : 0.0);
            requestData.put("fob_price", product.getFobPrice() != null ? product.getFobPrice() : 0.0);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);
            
            // precedents-analysis API 호출
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                precedentsAnalysisUrl + "/analyze-precedents",
                entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                if (result != null) {
                    log.info("판례 분석 완료 - 상품 ID: {}, 성공사례: {}개, 실패사례: {}개, 신뢰도: {}", 
                            product.getProductId(), 
                            ((java.util.List<?>) result.getOrDefault("success_cases", java.util.List.of())).size(),
                            ((java.util.List<?>) result.getOrDefault("failure_cases", java.util.List.of())).size(),
                            result.get("confidence_score"));
                    return result;
                }
            }
            log.error("판례 분석 실패 - 상품 ID: {}, 상태코드: {}", 
                    product.getProductId(), response.getStatusCode());
            return getDefaultPrecedentsResult();
            
        } catch (Exception e) {
            log.error("판례 분석 중 오류 발생 - 상품 ID: {}, 오류: {}", 
                     product.getProductId(), e.getMessage(), e);
            return getDefaultPrecedentsResult();
        }
    }

    /**
     * 기본 분석 결과 반환
     */
    private Map<String, Object> getDefaultAnalysisResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("success_cases", java.util.List.of());
        result.put("failure_cases", java.util.List.of());
        result.put("actionable_insights", java.util.List.of("AI 분석 서비스 연결 실패"));
        result.put("risk_factors", java.util.List.of("시스템 오류"));
        result.put("recommended_action", "관세사 상담 권장");
        result.put("confidence_score", 0.0);
        result.put("is_valid", false);
        return result;
    }

    /**
     * 요구사항 분석 실행
     */
    public Map<String, Object> executeRequirementsAnalysis(Product product) {
        try {
            log.info("요구사항 분석 실행 시작 - 상품 ID: {}, HS코드: {}", product.getProductId(), product.getHsCode());
            
            // 요청 데이터 구성
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("hs_code", product.getHsCode());
            requestData.put("product_name", product.getProductName());
            requestData.put("product_description", product.getDescription() != null ? product.getDescription() : "");
            requestData.put("target_country", "US");
            requestData.put("force_refresh", true);  // 캐시 무시하고 새로 분석
            requestData.put("is_new_product", false);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);
            
            // AI Engine 요구사항 분석 API 호출
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                requirementsAnalysisUrl + "/requirements/analyze",
                entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                if (result != null) {
                    log.info("요구사항 분석 완료 - 상품 ID: {}, 신뢰도: {}", 
                            product.getProductId(), result.get("metadata"));
                    return result;
                }
            }
            log.error("요구사항 분석 실패 - 상품 ID: {}, 상태코드: {}", 
                    product.getProductId(), response.getStatusCode());
            return getDefaultRequirementsResult();
            
        } catch (Exception e) {
            log.error("요구사항 분석 중 오류 발생 - 상품 ID: {}, 오류: {}", 
                     product.getProductId(), e.getMessage(), e);
            return getDefaultRequirementsResult();
        }
    }

    /**
     * 기본 판례 분석 결과 반환
     */
    private Map<String, Object> getDefaultPrecedentsResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("success_cases", java.util.List.of());
        result.put("failure_cases", java.util.List.of());
        result.put("actionable_insights", java.util.List.of("판례 분석 서비스 연결 실패"));
        result.put("risk_factors", java.util.List.of("시스템 오류"));
        result.put("recommended_action", "관세사 상담 권장");
        result.put("confidence_score", 0.0);
        result.put("is_valid", false);
        return result;
    }

    @Value("${ai.detailed-regulations-analysis.url:http://localhost:8000}")
    private String detailedRegulationsAnalysisUrl;

    @Value("${ai.penalties-analysis.url:http://localhost:8000}")
    private String penaltiesAnalysisUrl;

    @Value("${ai.testing-procedures-analysis.url:http://localhost:8000}")
    private String testingProceduresAnalysisUrl;

    @Value("${ai.validity-analysis.url:http://localhost:8000}")
    private String validityAnalysisUrl;

    /**
     * 세부 규정 분석 실행 (농약 잔류량, 화학성분 제한, 식품첨가물 기준, EMC 기준 등)
     */
    public Map<String, Object> executeDetailedRegulationsAnalysis(Product product) {
        try {
            log.info("세부 규정 분석 실행 시작 - 상품 ID: {}, HS코드: {}", product.getProductId(), product.getHsCode());
            
            // 요청 데이터 구성
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("hs_code", product.getHsCode());
            requestData.put("product_name", product.getProductName());
            requestData.put("product_description", product.getDescription() != null ? product.getDescription() : "");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);
            
            // AI Engine 세부 규정 분석 API 호출
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                detailedRegulationsAnalysisUrl + "/detailed-regulations/search",
                entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                if (result != null) {
                    log.info("세부 규정 분석 완료 - 상품 ID: {}, 카테고리: {}, 신뢰도: {}", 
                            product.getProductId(), 
                            result.get("category"),
                            result.get("confidence"));
                    return result;
                }
            }
            log.error("세부 규정 분석 실패 - 상품 ID: {}, 상태코드: {}", 
                    product.getProductId(), response.getStatusCode());
            return getDefaultDetailedRegulationsResult();
            
        } catch (Exception e) {
            log.error("세부 규정 분석 중 오류 발생 - 상품 ID: {}, 오류: {}", 
                     product.getProductId(), e.getMessage(), e);
            return getDefaultDetailedRegulationsResult();
        }
    }

    /**
     * 기본 세부 규정 분석 결과 반환
     */
    private Map<String, Object> getDefaultDetailedRegulationsResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("hs_code", "");
        result.put("product_name", "");
        result.put("category", "general");
        result.put("confidence", 0.0);
        result.put("search_timestamp", java.time.Instant.now().toString());
        result.put("phase_results", Map.of(
            "phase1_detailed_regulations", Map.of(),
            "phase2_testing_procedures", Map.of(),
            "phase3_penalties_enforcement", Map.of(),
            "phase4_validity_periods", Map.of()
        ));
        result.put("extracted_regulations", Map.of(
            "pesticide_residue_limits", java.util.List.of(),
            "chemical_restrictions", java.util.List.of(),
            "food_additive_standards", java.util.List.of(),
            "emc_standards", java.util.List.of(),
            "safety_standards", java.util.List.of()
        ));
        result.put("sources", java.util.List.of());
        result.put("error", true);
        result.put("error_message", "세부 규정 분석 서비스 연결 실패");
        return result;
    }

    /**
     * 기본 요구사항 분석 결과 반환
     */
    private Map<String, Object> getDefaultRequirementsResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("answer", "요구사항 분석 서비스 연결 실패");
        result.put("reasoning", "AI 엔진과의 연결에 문제가 발생했습니다.");
        result.put("requirements", Map.of(
            "certifications", java.util.List.of(),
            "documents", java.util.List.of(),
            "labeling", java.util.List.of(),
            "sources", java.util.List.of(),
            "metadata", Map.of("error", true, "error_message", "서비스 연결 실패")
        ));
        result.put("sources", java.util.List.of());
        result.put("metadata", Map.of(
            "confidence_score", 0.0,
            "error", true,
            "error_message", "AI 엔진 연결 실패"
        ));
        return result;
    }

    /**
     * 검사 절차 및 방법 분석 실행 (Phase 2)
     */
    public Map<String, Object> executeTestingProceduresAnalysis(Product product) {
        try {
            log.info("검사 절차 분석 실행 시작 - 상품 ID: {}, HS코드: {}", product.getProductId(), product.getHsCode());

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("hs_code", product.getHsCode());
            requestData.put("product_name", product.getProductName());
            requestData.put("product_description", product.getDescription() != null ? product.getDescription() : "");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                testingProceduresAnalysisUrl + "/testing-procedures/analyze",
                entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                log.info("검사 절차 분석 완료 - 상품 ID: {}, 주기: {}, 방법 수: {}", 
                        product.getProductId(),
                        result.getOrDefault("inspection_cycle", "unknown"),
                        ((java.util.List<?>) result.getOrDefault("methods", java.util.List.of())).size());
                return result;
            }

            log.error("검사 절차 분석 실패 - 상품 ID: {}, 상태코드: {}", product.getProductId(), response.getStatusCode());
            return getDefaultTestingProceduresResult();

        } catch (Exception e) {
            log.error("검사 절차 분석 중 오류 - 상품 ID: {}, 오류: {}", product.getProductId(), e.getMessage(), e);
            return getDefaultTestingProceduresResult();
        }
    }

    private Map<String, Object> getDefaultTestingProceduresResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("hs_code", "");
        result.put("product_name", "");
        result.put("analysis_timestamp", "");
        result.put("agencies", java.util.List.of());
        result.put("inspection_cycle", "unknown");
        result.put("methods", java.util.List.of());
        result.put("estimates", Map.of("estimated_cost_band", "unknown", "estimated_duration_band", "unknown"));
        result.put("evidence", Map.of());
        result.put("sources", java.util.List.of());
        result.put("error", true);
        result.put("error_message", "검사 절차 분석 서비스 연결 실패");
        return result;
    }

    /**
     * 처벌 및 벌금 분석 실행 (Phase 3)
     */
    public Map<String, Object> executePenaltiesAnalysis(Product product) {
        try {
            log.info("처벌/벌금 분석 실행 - 상품 ID: {}, HS: {}", product.getProductId(), product.getHsCode());

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("hs_code", product.getHsCode());
            requestData.put("product_name", product.getProductName());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                penaltiesAnalysisUrl + "/penalties/analyze",
                entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                log.info("처벌/벌금 분석 완료 - 상품 ID: {}, 기관 수: {}", product.getProductId(),
                        ((java.util.List<?>) result.getOrDefault("agencies", java.util.List.of())).size());
                return result;
            }
            log.error("처벌/벌금 분석 실패 - 상품 ID: {}, 상태코드: {}", product.getProductId(), response.getStatusCode());
            return Map.of("error", true, "message", "penalties service error");

        } catch (Exception e) {
            log.error("처벌/벌금 분석 오류 - 상품 ID: {}, 오류: {}", product.getProductId(), e.getMessage(), e);
            return Map.of("error", true, "message", e.getMessage());
        }
    }

    /**
     * 유효기간/갱신 분석 실행 (Phase 4)
     */
    public Map<String, Object> executeValidityAnalysis(Product product) {
        try {
            log.info("유효기간/갱신 분석 실행 - 상품 ID: {}, HS: {}", product.getProductId(), product.getHsCode());

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("hs_code", product.getHsCode());
            requestData.put("product_name", product.getProductName());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                validityAnalysisUrl + "/validity/analyze",
                entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                log.info("유효기간/갱신 분석 완료 - 상품 ID: {}, 기관 수: {}", product.getProductId(),
                        ((java.util.List<?>) result.getOrDefault("agencies", java.util.List.of())).size());
                return result;
            }
            log.error("유효기간/갱신 분석 실패 - 상품 ID: {}, 상태코드: {}", product.getProductId(), response.getStatusCode());
            return Map.of("error", true, "message", "validity service error");

        } catch (Exception e) {
            log.error("유효기간/갱신 분석 오류 - 상품 ID: {}, 오류: {}", product.getProductId(), e.getMessage(), e);
            return Map.of("error", true, "message", e.getMessage());
        }
    }
}