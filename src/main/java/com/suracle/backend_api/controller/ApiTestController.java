package com.suracle.backend_api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.suracle.backend_api.service.http.RequirementsApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * API 테스트 컨트롤러
 * Python과 동일한 API 호출을 테스트하여 결과 비교
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class ApiTestController {
    
    private final RequirementsApiClient requirementsApiClient;
    
    /**
     * FDA Cosmetic Event API 테스트
     */
    @GetMapping("/fda/cosmetic-event")
    public ResponseEntity<Map<String, Object>> testFdaCosmeticEvent(
            @RequestParam(defaultValue = "Premium Vitamin C Serum") String productName) {
        
        log.info("☕ [Spring Boot] FDA Cosmetic Event API 테스트: {}", productName);
        
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            Optional<JsonNode> response = requirementsApiClient.callOpenFdaCosmeticEvent(productName);
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (response.isPresent()) {
                JsonNode data = response.get();
                int resultCount = data.path("results").size();
                
                result.put("success", true);
                result.put("platform", "Spring Boot");
                result.put("api", "FDA Cosmetic Event");
                result.put("productName", productName);
                result.put("resultCount", resultCount);
                result.put("responseTimeMs", responseTime);
                result.put("timestamp", Instant.now().toString());
                result.put("endpoint", "https://api.fda.gov/cosmetics/event.json");
                
                log.info("✅ [Spring Boot] FDA Cosmetic Event 성공: {}개 결과 ({}ms)", resultCount, responseTime);
                
            } else {
                result.put("success", false);
                result.put("error", "API 호출 결과가 없습니다");
                result.put("responseTimeMs", responseTime);
                
                log.warn("❌ [Spring Boot] FDA Cosmetic Event 실패 ({}ms)", responseTime);
            }
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("responseTimeMs", responseTime);
            
            log.error("❌ [Spring Boot] FDA Cosmetic Event 예외 발생: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * EPA CompTox Chemical Search API 테스트
     */
    @GetMapping("/epa/comptox")
    public ResponseEntity<Map<String, Object>> testEpaCompTox(
            @RequestParam(defaultValue = "ascorbic acid") String query) {
        
        log.info("☕ [Spring Boot] EPA CompTox API 테스트: {}", query);
        
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            Optional<JsonNode> response = requirementsApiClient.callEpaCompToxSearch(query);
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (response.isPresent()) {
                JsonNode data = response.get();
                int resultCount = data.path("results").size();
                
                result.put("success", true);
                result.put("platform", "Spring Boot");
                result.put("api", "EPA CompTox Chemical Search");
                result.put("query", query);
                result.put("resultCount", resultCount);
                result.put("responseTimeMs", responseTime);
                result.put("timestamp", Instant.now().toString());
                result.put("endpoint", "https://comptox.epa.gov/dashboard/api/chemical/search");
                
                log.info("✅ [Spring Boot] EPA CompTox 성공: {}개 결과 ({}ms)", resultCount, responseTime);
                
            } else {
                result.put("success", false);
                result.put("error", "API 호출 결과가 없습니다");
                result.put("responseTimeMs", responseTime);
                
                log.warn("❌ [Spring Boot] EPA CompTox 실패 ({}ms)", responseTime);
            }
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("responseTimeMs", responseTime);
            
            log.error("❌ [Spring Boot] EPA CompTox 예외 발생: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * EPA Envirofacts Chemical Search API 테스트 (새로운 엔드포인트)
     */
    @GetMapping("/epa/envirofacts")
    public ResponseEntity<Map<String, Object>> testEpaEnvirofacts(
            @RequestParam(defaultValue = "ascorbic acid") String query) {
        
        log.info("☕ [Spring Boot] EPA Envirofacts API 테스트: {}", query);
        
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            Optional<JsonNode> response = requirementsApiClient.callEpaEnvirofactsSearch(query);
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (response.isPresent()) {
                JsonNode data = response.get();
                int resultCount = data.isArray() ? data.size() : 1;
                
                result.put("success", true);
                result.put("platform", "Spring Boot");
                result.put("api", "EPA Envirofacts Chemical Search");
                result.put("query", query);
                result.put("resultCount", resultCount);
                result.put("responseTimeMs", responseTime);
                result.put("timestamp", Instant.now().toString());
                result.put("endpoint", "https://data.epa.gov/efservice/srs.srs_chemicals/chem_name/LIKE/{query}/JSON");
                
                log.info("✅ [Spring Boot] EPA Envirofacts 성공: {}개 결과 ({}ms)", resultCount, responseTime);
                
            } else {
                result.put("success", false);
                result.put("error", "API 호출 결과가 없습니다");
                result.put("responseTimeMs", responseTime);
                
                log.warn("❌ [Spring Boot] EPA Envirofacts 실패 ({}ms)", responseTime);
            }
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("responseTimeMs", responseTime);
            
            log.error("❌ [Spring Boot] EPA Envirofacts 예외 발생: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * USDA FoodData Central API 테스트
     */
    @GetMapping("/usda/fooddata-central")
    public ResponseEntity<Map<String, Object>> testUsdaFoodDataCentral(
            @RequestParam(defaultValue = "vitamin c serum") String query) {
        
        log.info("☕ [Spring Boot] USDA FoodData Central API 테스트: {}", query);
        
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            Optional<JsonNode> response = requirementsApiClient.callUsdaFoodDataCentralSearch(query, null);
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (response.isPresent()) {
                JsonNode data = response.get();
                int resultCount = data.path("foods").size();
                
                result.put("success", true);
                result.put("platform", "Spring Boot");
                result.put("api", "USDA FoodData Central");
                result.put("query", query);
                result.put("resultCount", resultCount);
                result.put("responseTimeMs", responseTime);
                result.put("timestamp", Instant.now().toString());
                result.put("endpoint", "https://api.nal.usda.gov/fdc/v1/foods/search");
                
                log.info("✅ [Spring Boot] USDA FoodData Central 성공: {}개 결과 ({}ms)", resultCount, responseTime);
                
            } else {
                result.put("success", false);
                result.put("error", "API 호출 결과가 없습니다");
                result.put("responseTimeMs", responseTime);
                
                log.warn("❌ [Spring Boot] USDA FoodData Central 실패 ({}ms)", responseTime);
            }
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("responseTimeMs", responseTime);
            
            log.error("❌ [Spring Boot] USDA FoodData Central 예외 발생: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * CPSC Recalls API 테스트
     */
    @GetMapping("/cpsc/recalls")
    public ResponseEntity<Map<String, Object>> testCpscRecalls(
            @RequestParam(defaultValue = "vitamin c serum") String productName) {
        
        log.info("☕ [Spring Boot] CPSC Recalls API 테스트: {}", productName);
        
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            Optional<JsonNode> response = requirementsApiClient.callCpscRecallsJson(productName);
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (response.isPresent()) {
                JsonNode data = response.get();
                int resultCount = data.path("results").size();
                
                result.put("success", true);
                result.put("platform", "Spring Boot");
                result.put("api", "CPSC Recalls");
                result.put("productName", productName);
                result.put("resultCount", resultCount);
                result.put("responseTimeMs", responseTime);
                result.put("timestamp", Instant.now().toString());
                result.put("endpoint", "https://www.cpsc.gov/Recalls/CPSC-Recalls-API/recalls.json");
                
                log.info("✅ [Spring Boot] CPSC Recalls 성공: {}개 결과 ({}ms)", resultCount, responseTime);
                
            } else {
                result.put("success", false);
                result.put("error", "API 호출 결과가 없습니다");
                result.put("responseTimeMs", responseTime);
                
                log.warn("❌ [Spring Boot] CPSC Recalls 실패 ({}ms)", responseTime);
            }
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("responseTimeMs", responseTime);
            
            log.error("❌ [Spring Boot] CPSC Recalls 예외 발생: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * FCC Device Authorization API 테스트 (새로운 엔드포인트)
     */
    @GetMapping("/fcc/device-authorization")
    public ResponseEntity<Map<String, Object>> testFccDeviceAuthorization(
            @RequestParam(defaultValue = "laptop") String deviceName) {
        
        log.info("☕ [Spring Boot] FCC API 테스트: {}", deviceName);
        
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            Optional<JsonNode> response = requirementsApiClient.callFccDeviceAuthorizationGrants(deviceName);
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (response.isPresent()) {
                JsonNode data = response.get();
                int resultCount = data.isArray() ? data.size() : 1;
                
                result.put("success", true);
                result.put("platform", "Spring Boot");
                result.put("api", "FCC Public Files API");
                result.put("deviceName", deviceName);
                result.put("resultCount", resultCount);
                result.put("responseTimeMs", responseTime);
                result.put("timestamp", Instant.now().toString());
                result.put("endpoint", "https://publicfiles.fcc.gov/api/search");
                
                log.info("✅ [Spring Boot] FCC API 성공: {}개 결과 ({}ms)", resultCount, responseTime);
                
            } else {
                result.put("success", false);
                result.put("error", "API 호출 결과가 없습니다");
                result.put("responseTimeMs", responseTime);
                
                log.warn("❌ [Spring Boot] FCC API 실패 ({}ms)", responseTime);
            }
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("responseTimeMs", responseTime);
            
            log.error("❌ [Spring Boot] FCC API 예외 발생: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * CBP Trade Statistics API 테스트
     */
    @GetMapping("/cbp/trade-statistics")
    public ResponseEntity<Map<String, Object>> testCbpTradeStatistics(
            @RequestParam(defaultValue = "8471") String hsCode) {
        
        log.info("☕ [Spring Boot] CBP Trade Statistics API 테스트: {}", hsCode);
        
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            Optional<JsonNode> response = requirementsApiClient.callCbpTradeStatisticsHsCodes(hsCode, null);
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (response.isPresent()) {
                JsonNode data = response.get();
                int resultCount = data.path("data").size();
                
                result.put("success", true);
                result.put("platform", "Spring Boot");
                result.put("api", "CBP Trade Statistics");
                result.put("hsCode", hsCode);
                result.put("resultCount", resultCount);
                result.put("responseTimeMs", responseTime);
                result.put("timestamp", Instant.now().toString());
                result.put("endpoint", "https://api.cbp.gov/trade/statistics/hs-codes");
                
                log.info("✅ [Spring Boot] CBP Trade Statistics 성공: {}개 결과 ({}ms)", resultCount, responseTime);
                
            } else {
                result.put("success", false);
                result.put("error", "API 호출 결과가 없습니다");
                result.put("responseTimeMs", responseTime);
                
                log.warn("❌ [Spring Boot] CBP Trade Statistics 실패 ({}ms)", responseTime);
            }
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("responseTimeMs", responseTime);
            
            log.error("❌ [Spring Boot] CBP Trade Statistics 예외 발생: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 모든 API 테스트 실행
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> testAllApis() {
        log.info("☕ [Spring Boot] 모든 API 테스트 시작");
        
        Map<String, Object> allResults = new HashMap<>();
        allResults.put("timestamp", Instant.now().toString());
        allResults.put("platform", "Spring Boot");
        
        // FDA Cosmetic Event 테스트
        ResponseEntity<Map<String, Object>> fdaResult = testFdaCosmeticEvent("Premium Vitamin C Serum");
        allResults.put("fda_cosmetic_event", fdaResult.getBody());
        
        // EPA CompTox 테스트
        ResponseEntity<Map<String, Object>> epaResult = testEpaCompTox("ascorbic acid");
        allResults.put("epa_comptox", epaResult.getBody());
        
        // USDA FoodData Central 테스트
        ResponseEntity<Map<String, Object>> usdaResult = testUsdaFoodDataCentral("vitamin c serum");
        allResults.put("usda_fooddata", usdaResult.getBody());
        
        // CPSC Recalls 테스트
        ResponseEntity<Map<String, Object>> cpscResult = testCpscRecalls("vitamin c serum");
        allResults.put("cpsc_recalls", cpscResult.getBody());
        
        // 요약 정보
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalTests", 4);
        summary.put("successfulTests", 0);
        summary.put("failedTests", 0);
        
        // 성공/실패 카운트
        for (Map.Entry<String, Object> entry : allResults.entrySet()) {
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> testResult = (Map<String, Object>) entry.getValue();
                if (Boolean.TRUE.equals(testResult.get("success"))) {
                    summary.put("successfulTests", (Integer) summary.get("successfulTests") + 1);
                } else {
                    summary.put("failedTests", (Integer) summary.get("failedTests") + 1);
                }
            }
        }
        
        allResults.put("summary", summary);
        
        log.info("📊 [Spring Boot] 모든 테스트 완료: {}/{} 성공", 
                summary.get("successfulTests"), summary.get("totalTests"));
        
        return ResponseEntity.ok(allResults);
    }

    // NTIA Spectrum Data API 테스트
    @GetMapping("/ntia/spectrum-data")
    public ResponseEntity<Map<String, Object>> testNtiaSpectrumData(
            @RequestParam(defaultValue = "5g") String query) {
        log.info("☕ [Spring Boot] NTIA Spectrum Data API 테스트: {}", query);
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        Optional<JsonNode> response = requirementsApiClient.callNtiaSpectrumData(query);
        long responseTime = System.currentTimeMillis() - startTime;
        
        result.put("success", response.isPresent());
        result.put("platform", "Spring Boot");
        result.put("api", "NTIA Spectrum Data");
        result.put("query", query);
        result.put("resultCount", response.map(JsonNode::size).orElse(0));
        result.put("responseTimeMs", responseTime);
        result.put("timestamp", Instant.now().toString());
        result.put("endpoint", "https://www.ntia.gov/data/spectrum-map");
        
        if (response.isPresent()) {
            log.info("✅ [Spring Boot] NTIA Spectrum Data 성공 ({}ms)", responseTime);
        } else {
            log.warn("❌ [Spring Boot] NTIA Spectrum Data 실패 ({}ms)", responseTime);
            result.put("error", "API 호출 결과가 없습니다");
        }
        
        return ResponseEntity.ok(result);
    }

    // DOT Safety Data API 테스트
    @GetMapping("/dot/safety-data")
    public ResponseEntity<Map<String, Object>> testDotSafetyData(
            @RequestParam(defaultValue = "vehicle") String query) {
        log.info("☕ [Spring Boot] DOT Safety Data API 테스트: {}", query);
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        Optional<JsonNode> response = requirementsApiClient.callDotSafetyData(query);
        long responseTime = System.currentTimeMillis() - startTime;
        
        result.put("success", response.isPresent());
        result.put("platform", "Spring Boot");
        result.put("api", "DOT Safety Data");
        result.put("query", query);
        result.put("resultCount", response.map(JsonNode::size).orElse(0));
        result.put("responseTimeMs", responseTime);
        result.put("timestamp", Instant.now().toString());
        result.put("endpoint", "https://www.nhtsa.gov/api");
        
        if (response.isPresent()) {
            log.info("✅ [Spring Boot] DOT Safety Data 성공 ({}ms)", responseTime);
        } else {
            log.warn("❌ [Spring Boot] DOT Safety Data 실패 ({}ms)", responseTime);
            result.put("error", "API 호출 결과가 없습니다");
        }
        
        return ResponseEntity.ok(result);
    }

    // DOE Energy Data API 테스트
    @GetMapping("/doe/energy-data")
    public ResponseEntity<Map<String, Object>> testDoeEnergyData(
            @RequestParam(defaultValue = "petroleum") String query) {
        log.info("☕ [Spring Boot] DOE Energy Data API 테스트: {}", query);
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        Optional<JsonNode> response = requirementsApiClient.callDoeEnergyData(query);
        long responseTime = System.currentTimeMillis() - startTime;
        
        result.put("success", response.isPresent());
        result.put("platform", "Spring Boot");
        result.put("api", "DOE Energy Data");
        result.put("query", query);
        result.put("resultCount", response.map(JsonNode::size).orElse(0));
        result.put("responseTimeMs", responseTime);
        result.put("timestamp", Instant.now().toString());
        result.put("endpoint", "https://api.eia.gov/petroleum");
        
        if (response.isPresent()) {
            log.info("✅ [Spring Boot] DOE Energy Data 성공 ({}ms)", responseTime);
        } else {
            log.warn("❌ [Spring Boot] DOE Energy Data 실패 ({}ms)", responseTime);
            result.put("error", "API 호출 결과가 없습니다");
        }
        
        return ResponseEntity.ok(result);
    }

    // DOI Natural Resources API 테스트
    @GetMapping("/doi/natural-resources")
    public ResponseEntity<Map<String, Object>> testDoiNaturalResources(
            @RequestParam(defaultValue = "minerals") String query) {
        log.info("☕ [Spring Boot] DOI Natural Resources API 테스트: {}", query);
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        Optional<JsonNode> response = requirementsApiClient.callDoiNaturalResources(query);
        long responseTime = System.currentTimeMillis() - startTime;
        
        result.put("success", response.isPresent());
        result.put("platform", "Spring Boot");
        result.put("api", "DOI Natural Resources");
        result.put("query", query);
        result.put("resultCount", response.map(JsonNode::size).orElse(0));
        result.put("responseTimeMs", responseTime);
        result.put("timestamp", Instant.now().toString());
        result.put("endpoint", "https://data.doi.gov/Minerals");
        
        if (response.isPresent()) {
            log.info("✅ [Spring Boot] DOI Natural Resources 성공 ({}ms)", responseTime);
        } else {
            log.warn("❌ [Spring Boot] DOI Natural Resources 실패 ({}ms)", responseTime);
            result.put("error", "API 호출 결과가 없습니다");
        }
        
        return ResponseEntity.ok(result);
    }

    // DOL Employment Data API 테스트
    @GetMapping("/dol/employment-data")
    public ResponseEntity<Map<String, Object>> testDolEmploymentData(
            @RequestParam(defaultValue = "unemployment") String query) {
        log.info("☕ [Spring Boot] DOL Employment Data API 테스트: {}", query);
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        Optional<JsonNode> response = requirementsApiClient.callDolEmploymentData(query);
        long responseTime = System.currentTimeMillis() - startTime;
        
        result.put("success", response.isPresent());
        result.put("platform", "Spring Boot");
        result.put("api", "DOL Employment Data");
        result.put("query", query);
        result.put("resultCount", response.map(JsonNode::size).orElse(0));
        result.put("responseTimeMs", responseTime);
        result.put("timestamp", Instant.now().toString());
        result.put("endpoint", "https://api.dol.gov/unemployment");
        
        if (response.isPresent()) {
            log.info("✅ [Spring Boot] DOL Employment Data 성공 ({}ms)", responseTime);
        } else {
            log.warn("❌ [Spring Boot] DOL Employment Data 실패 ({}ms)", responseTime);
            result.put("error", "API 호출 결과가 없습니다");
        }
        
        return ResponseEntity.ok(result);
    }
}
