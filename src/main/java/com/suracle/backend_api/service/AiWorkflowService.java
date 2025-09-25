package com.suracle.backend_api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;
    
    @Value("${ai.workflow.url:http://localhost:8000}")
    private String aiWorkflowUrl;
    
    @Value("${ai.precedents-analysis.url:http://localhost:8000}")
    private String precedentsAnalysisUrl;

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
            ResponseEntity<Map> response = restTemplate.postForEntity(
                precedentsAnalysisUrl + "/analyze-precedents",
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                log.info("AI 워크플로우 실행 완료 - 상품 ID: {}, 신뢰도: {}", 
                        product.getProductId(), result.get("confidence_score"));
                return result;
            } else {
                log.error("AI 워크플로우 실행 실패 - 상품 ID: {}, 상태코드: {}", 
                        product.getProductId(), response.getStatusCode());
                return getDefaultAnalysisResult();
            }
            
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
            ResponseEntity<Map> response = restTemplate.postForEntity(
                precedentsAnalysisUrl + "/analyze-precedents",
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                log.info("판례 분석 완료 - 상품 ID: {}, 성공사례: {}개, 실패사례: {}개, 신뢰도: {}", 
                        product.getProductId(), 
                        ((java.util.List<?>) result.getOrDefault("success_cases", java.util.List.of())).size(),
                        ((java.util.List<?>) result.getOrDefault("failure_cases", java.util.List.of())).size(),
                        result.get("confidence_score"));
                return result;
            } else {
                log.error("판례 분석 실패 - 상품 ID: {}, 상태코드: {}", 
                        product.getProductId(), response.getStatusCode());
                return getDefaultPrecedentsResult();
            }
            
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
}
