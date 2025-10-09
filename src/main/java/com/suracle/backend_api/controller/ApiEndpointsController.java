package com.suracle.backend_api.controller;

import com.suracle.backend_api.service.http.RequirementsApiClient;
import com.suracle.backend_api.config.ApiEndpointsManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * API 엔드포인트 정보를 제공하는 컨트롤러
 * Python의 api_endpoints.py와 동일한 정보를 Java로 제공
 */
@RestController
@RequestMapping("/api/endpoints")
@RequiredArgsConstructor
@Slf4j
public class ApiEndpointsController {
    
    private final RequirementsApiClient requirementsApiClient;
    private final ApiEndpointsManager apiEndpointsManager;
    
    /**
     * 모든 API 엔드포인트 정보 조회
     * Python의 api_endpoints.py와 동일한 구조로 반환
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getApiEndpointsInfo() {
        try {
            log.info("🔍 API 엔드포인트 정보 조회 요청");
            
            Map<String, Object> endpointsInfo = requirementsApiClient.getApiEndpointsInfo();
            
            log.info("✅ API 엔드포인트 정보 조회 완료: {}개 기관", 
                    ((java.util.List<?>) endpointsInfo.get("agencies")).size());
            
            return ResponseEntity.ok(endpointsInfo);
            
        } catch (Exception e) {
            log.error("❌ API 엔드포인트 정보 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 지정된 agency/category/endpoint를 해석하여 최종 URL과 메타데이터 반환
     */
    @GetMapping("/resolve")
    public ResponseEntity<Map<String, Object>> resolveEndpoint(
            @org.springframework.web.bind.annotation.RequestParam String agency,
            @org.springframework.web.bind.annotation.RequestParam String category,
            @org.springframework.web.bind.annotation.RequestParam String endpoint
    ) {
        try {
            String url = apiEndpointsManager.getEndpoint(agency, category, endpoint);
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("agency", agency);
            body.put("category", category);
            body.put("endpoint", endpoint);
            body.put("url", url);
            body.put("baseUrl", apiEndpointsManager.getBaseUrl(agency));
            body.put("apiKeyRequired", apiEndpointsManager.isApiKeyRequired(agency));
            body.put("rateLimit", apiEndpointsManager.getRateLimit(agency));
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("❌ 엔드포인트 해석 실패: {}.{}.{} - {}", agency, category, endpoint, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 전체 기관별 엔드포인트 카탈로그 반환
     */
    @GetMapping("/catalog")
    public ResponseEntity<Map<String, Object>> getCatalog() {
        try {
            Map<String, Object> body = new java.util.HashMap<>();
            var agencies = apiEndpointsManager.getAllAgencies();
            body.put("agencies", agencies);
            // 각 기관의 엔드포인트 맵 포함
            Map<String, Object> catalog = new java.util.HashMap<>();
            for (String agencyKey : agencies) {
                catalog.put(agencyKey, apiEndpointsManager.getAllEndpoints(agencyKey));
            }
            body.put("catalog", catalog);
            body.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("❌ 엔드포인트 카탈로그 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * API 엔드포인트 상태 확인 (헬스체크)
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> healthInfo = Map.of(
                "status", "UP",
                "timestamp", java.time.Instant.now().toString(),
                "message", "API 엔드포인트 관리 시스템 정상 동작 중",
                "pythonSync", "Python api_endpoints.py와 동기화됨"
            );
            
            log.info("✅ API 엔드포인트 헬스체크 완료");
            return ResponseEntity.ok(healthInfo);
            
        } catch (Exception e) {
            log.error("❌ API 엔드포인트 헬스체크 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
