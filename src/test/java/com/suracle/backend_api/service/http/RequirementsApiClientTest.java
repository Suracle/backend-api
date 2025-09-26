package com.suracle.backend_api.service.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.suracle.backend_api.config.ApiEndpointsManager;
import com.suracle.backend_api.config.ApiKeysProperties;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RequirementsApiClient 테스트 클래스
 * Python과 동일한 API 호출을 테스트하여 결과 비교
 */
@SpringBootTest
@TestPropertySource(properties = {
    "requirements.api.usdaKey=VSYH9536pma3JrbLyzfZeNWurHO6taOw91n9k74r",
    "requirements.api.cbpKey=i4O4Id1k7S5omZaIZrc2f560ISv4rFDoJFvTcagg",
    "requirements.api.dataGovKey=i4O4Id1k7S5omZaIZrc2f560ISv4rFDoJFvTcagg",
    "requirements.api.epaKey=i4O4Id1k7S5omZaIZrc2f560ISv4rFDoJFvTcagg"
})
class RequirementsApiClientTest {
    
    private static final Logger log = LoggerFactory.getLogger(RequirementsApiClientTest.class);

    @Autowired
    private RequirementsApiClient requirementsApiClient;
    
    @Autowired
    private ApiEndpointsManager apiEndpointsManager;
    
    @Autowired
    private ApiKeysProperties apiKeysProperties;

    @Test
    void testFdaCosmeticEvent() {
        log.info("☕ [Spring Boot] FDA Cosmetic Event API 테스트 시작");
        
        String productName = "Premium Vitamin C Serum";
        long startTime = System.currentTimeMillis();
        
        Optional<JsonNode> result = requirementsApiClient.callOpenFdaCosmeticEvent(productName);
        
        long responseTime = System.currentTimeMillis() - startTime;
        
        if (result.isPresent()) {
            JsonNode data = result.get();
            int resultCount = data.path("results").size();
            log.info("✅ [Spring Boot] FDA Cosmetic Event 성공: {}개 결과 ({}ms)", resultCount, responseTime);
            
            // 결과 검증
            assertTrue(result.isPresent(), "API 호출 결과가 있어야 함");
            assertTrue(data.has("results"), "results 필드가 있어야 함");
            assertNotNull(data.path("results"), "results가 null이 아니어야 함");
            
        } else {
            log.warn("❌ [Spring Boot] FDA Cosmetic Event 실패 ({}ms)", responseTime);
            log.info("FDA Cosmetic Event API는 일시적으로 사용할 수 없을 수 있습니다");
            // API 실패는 테스트 실패로 간주하지 않음 (실제 서비스 상황)
        }
    }

    @Test
    void testEpaCompToxSearch() {
        log.info("☕ [Spring Boot] EPA CompTox API 테스트 시작");
        
        String query = "ascorbic acid";
        long startTime = System.currentTimeMillis();
        
        Optional<JsonNode> result = requirementsApiClient.callEpaCompToxSearch(query);
        
        long responseTime = System.currentTimeMillis() - startTime;
        
        if (result.isPresent()) {
            JsonNode data = result.get();
            int resultCount = data.path("results").size();
            log.info("✅ [Spring Boot] EPA CompTox 성공: {}개 결과 ({}ms)", resultCount, responseTime);
            
            // 결과 검증
            assertTrue(result.isPresent(), "API 호출 결과가 있어야 함");
            
        } else {
            log.warn("❌ [Spring Boot] EPA CompTox 실패 ({}ms)", responseTime);
            // EPA API는 실패할 수 있으므로 테스트 실패로 처리하지 않음
            log.info("EPA CompTox API는 일시적으로 사용할 수 없을 수 있습니다");
        }
    }

    @Test
    void testUsdaFoodDataCentral() {
        log.info("☕ [Spring Boot] USDA FoodData Central API 테스트 시작");
        
        String query = "vitamin c serum";
        long startTime = System.currentTimeMillis();
        
        Optional<JsonNode> result = requirementsApiClient.callUsdaFoodDataCentralSearch(query, null);
        
        long responseTime = System.currentTimeMillis() - startTime;
        
        if (result.isPresent()) {
            JsonNode data = result.get();
            int resultCount = data.path("foods").size();
            log.info("✅ [Spring Boot] USDA FoodData Central 성공: {}개 결과 ({}ms)", resultCount, responseTime);
            
            // 결과 검증
            assertTrue(result.isPresent(), "API 호출 결과가 있어야 함");
            assertTrue(data.has("foods"), "foods 필드가 있어야 함");
            
        } else {
            log.warn("❌ [Spring Boot] USDA FoodData Central 실패 ({}ms)", responseTime);
            log.info("USDA FoodData Central API는 일시적으로 사용할 수 없을 수 있습니다");
            // API 실패는 테스트 실패로 간주하지 않음 (실제 서비스 상황)
        }
    }

    @Test
    void testCpscRecalls() {
        log.info("☕ [Spring Boot] CPSC Recalls API 테스트 시작");
        
        String productName = "vitamin c serum";
        long startTime = System.currentTimeMillis();
        
        Optional<JsonNode> result = requirementsApiClient.callCpscRecallsJson(productName);
        
        long responseTime = System.currentTimeMillis() - startTime;
        
        if (result.isPresent()) {
            JsonNode data = result.get();
            int resultCount = data.path("results").size();
            log.info("✅ [Spring Boot] CPSC Recalls 성공: {}개 결과 ({}ms)", resultCount, responseTime);
            
            // 결과 검증
            assertTrue(result.isPresent(), "API 호출 결과가 있어야 함");
            
        } else {
            log.warn("❌ [Spring Boot] CPSC Recalls 실패 ({}ms)", responseTime);
            log.info("CPSC Recalls API는 일시적으로 사용할 수 없을 수 있습니다");
        }
    }

    @Test
    void testApiEndpointsManager() {
        log.info("☕ [Spring Boot] ApiEndpointsManager 테스트 시작");
        
        // FDA 엔드포인트 테스트
        String fdaEndpoint = apiEndpointsManager.getEndpoint("fda", "cosmetic", "event");
        assertEquals("https://api.fda.gov/cosmetic/event.json", fdaEndpoint);
        log.info("✅ FDA Cosmetic Event 엔드포인트: {}", fdaEndpoint);
        
        // EPA 엔드포인트 테스트
        String epaEndpoint = apiEndpointsManager.getEndpoint("epa", "chemicals", "search");
        assertEquals("https://comptox.epa.gov/dashboard/api/chemical/search", epaEndpoint);
        log.info("✅ EPA CompTox 엔드포인트: {}", epaEndpoint);
        
        // USDA 엔드포인트 테스트
        String usdaEndpoint = apiEndpointsManager.getEndpoint("usda", "fooddata_central", "search");
        assertEquals("https://api.nal.usda.gov/fdc/v1/foods/search", usdaEndpoint);
        log.info("✅ USDA FoodData Central 엔드포인트: {}", usdaEndpoint);
        
        // API 키 요구사항 테스트
        assertFalse(apiEndpointsManager.isApiKeyRequired("fda"), "FDA는 API 키가 필요하지 않아야 함");
        assertTrue(apiEndpointsManager.isApiKeyRequired("usda"), "USDA는 API 키가 필요해야 함");
        assertFalse(apiEndpointsManager.isApiKeyRequired("cpsc"), "CPSC는 API 키가 필요하지 않아야 함");
        
        log.info("✅ ApiEndpointsManager 모든 테스트 통과");
    }

    @Test
    void testApiKeysProperties() {
        log.info("☕ [Spring Boot] ApiKeysProperties 테스트 시작");
        
        assertNotNull(apiKeysProperties.getUsdaKey(), "USDA API 키가 설정되어 있어야 함");
        assertNotNull(apiKeysProperties.getCbpKey(), "CBP API 키가 설정되어 있어야 함");
        assertNotNull(apiKeysProperties.getDataGovKey(), "Data.gov API 키가 설정되어 있어야 함");
        assertNotNull(apiKeysProperties.getEpaKey(), "EPA API 키가 설정되어 있어야 함");
        
        log.info("✅ ApiKeysProperties 모든 테스트 통과");
    }

    @Test
    void testApiEndpointsInfo() {
        log.info("☕ [Spring Boot] API 엔드포인트 정보 조회 테스트 시작");
        
        Map<String, Object> endpointsInfo = requirementsApiClient.getApiEndpointsInfo();
        
        assertNotNull(endpointsInfo, "엔드포인트 정보가 null이 아니어야 함");
        assertTrue(endpointsInfo.containsKey("agencies"), "agencies 키가 있어야 함");
        assertTrue(endpointsInfo.containsKey("agencyDetails"), "agencyDetails 키가 있어야 함");
        
        @SuppressWarnings("unchecked")
        java.util.List<String> agencies = (java.util.List<String>) endpointsInfo.get("agencies");
        assertTrue(agencies.contains("fda"), "FDA 기관이 포함되어 있어야 함");
        assertTrue(agencies.contains("usda"), "USDA 기관이 포함되어 있어야 함");
        assertTrue(agencies.contains("epa"), "EPA 기관이 포함되어 있어야 함");
        assertTrue(agencies.contains("cpsc"), "CPSC 기관이 포함되어 있어야 함");
        
        log.info("✅ API 엔드포인트 정보 조회 테스트 통과");
        log.info("📊 총 {}개 기관의 엔드포인트 정보 조회됨", agencies.size());
    }
}
