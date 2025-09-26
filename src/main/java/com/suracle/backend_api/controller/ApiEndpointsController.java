package com.suracle.backend_api.controller;

import com.suracle.backend_api.service.http.RequirementsApiClient;
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
