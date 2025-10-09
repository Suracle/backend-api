package com.suracle.backend_api.service.http;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

/**
 * Requirements API 클라이언트
 * 다양한 정부 기관 API 호출을 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RequirementsApiClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${requirements.api.usdaKey:}")
    private String usdaKey;
    
    @Value("${requirements.api.cbpKey:}")
    private String cbpKey;
    
    @Value("${requirements.api.dataGovKey:}")
    private String dataGovKey;
    
    @Value("${requirements.api.epaKey:}")
    private String epaKey;
    
    @Value("${requirements.api.censusKey:}")
    private String censusKey;
    
    /**
     * FDA Cosmetics Event API 호출 (복수형 경로)
     * 쿼리 필드 강화: brand_name + product_description 병행 검색
     */
    public Optional<JsonNode> callOpenFdaCosmeticEvent(String productName) {
        try {
            String url = "https://api.fda.gov/cosmetics/event.json";
            // 브랜드명 또는 제품 설명에서 검색
            String searchQuery = "products.brand_name:\"" + productName + "\" OR products.product_description:\"" + productName + "\"";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("search", searchQuery)
                    .queryParam("limit", 10);
            
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                    builder.toUriString(), JsonNode.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("FDA Cosmetics Event API 호출 실패: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * EPA CompTox Chemical Search API 호출
     */
    public Optional<JsonNode> callEpaCompToxSearch(String query) {
        try {
            String url = "https://comptox.epa.gov/dashboard/api/chemical/search";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("search", query)
                    .queryParam("limit", 10);
            
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                    builder.toUriString(), JsonNode.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("EPA CompTox API 호출 실패: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * EPA Envirofacts Chemical Search API 호출
     */
    public Optional<JsonNode> callEpaEnvirofactsSearch(String query) {
        try {
            String url = "https://data.epa.gov/efservice/srs.srs_chemicals/chem_name/LIKE/{query}/JSON";
            String formattedUrl = url.replace("{query}", query);
            
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                    formattedUrl, JsonNode.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("EPA Envirofacts API 호출 실패: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * USDA FoodData Central API 호출
     */
    public Optional<JsonNode> callUsdaFoodDataCentralSearch(String query, String dataType) {
        try {
            String url = "https://api.nal.usda.gov/fdc/v1/foods/search";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("api_key", usdaKey)
                    .queryParam("query", query)
                    .queryParam("pageSize", 10);
            
            if (dataType != null && !dataType.isEmpty()) {
                builder.queryParam("dataType", dataType);
            }
            
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                    builder.toUriString(), JsonNode.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("USDA FoodData Central API 호출 실패: {}", e.getMessage());
        }
        
        return Optional.empty();
    }

    /**
     * FDA Food Enforcement API 호출
     */
    public Optional<JsonNode> callOpenFdaFoodEnforcement(String productName) {
        try {
            String url = "https://api.fda.gov/food/enforcement.json";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("search", "product_description:\"" + productName + "\"")
                    .queryParam("limit", 10);

            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                    builder.toUriString(), JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }

        } catch (Exception e) {
            log.error("FDA Food Enforcement API 호출 실패: {}", e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * FDA Food Event API 호출
     * ⚠️ 주의: openFDA에는 /food/event.json 엔드포인트가 존재하지 않음
     * 푸드 관련은 food/enforcement.json만 사용
     */
    @Deprecated
    public Optional<JsonNode> callOpenFdaFoodEvent(String productName) {
        log.warn("FDA Food Event API는 존재하지 않는 엔드포인트입니다. food/enforcement를 사용하세요.");
        return Optional.empty();
    }

    /**
     * EPA SRS Chemname API 호출
     */
    public Optional<JsonNode> callEpaSrsChemname(String chemname) {
        try {
            String url = "https://cdxapps.epa.gov/ords/srs/srs_api/chemname/" + chemname;
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
        } catch (Exception e) {
            log.error("EPA SRS Chemname API 호출 실패: {}", e.getMessage());
        }
        return Optional.empty();
    }
    
    /**
     * CPSC Recalls API 호출
     */
    public Optional<JsonNode> callCpscRecallsJson(String productName) {
        try {
            String url = "https://www.cpsc.gov/Recalls/CPSC-Recalls-API/recalls.json";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("search", productName)
                    .queryParam("limit", 10);
            
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                    builder.toUriString(), JsonNode.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("CPSC Recalls API 호출 실패: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * FCC Device Authorization API 호출 (OpenData Socrata)
     */
    public Optional<JsonNode> callFccDeviceAuthorizationGrants(String deviceName) {
        try {
            String url = "https://opendata.fcc.gov/resource/3b3k-34jp.json";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("$select", "grantee_code,grantee_name,state")
                    .queryParam("$where", "upper(grantee_name) like '%" + deviceName.toUpperCase() + "%'")
                    .queryParam("$limit", 10);
            
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                    builder.toUriString(), JsonNode.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("FCC Device Authorization API 호출 실패: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * CBP Trade Statistics API 호출
     */
    public Optional<JsonNode> callCbpTradeStatisticsHsCodes(String hsCode, String country) {
        try {
            String url = "https://api.cbp.gov/trade/statistics/hs-codes";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("hsCode", hsCode)
                    .queryParam("limit", 10);
            
            if (country != null && !country.isEmpty()) {
                builder.queryParam("country", country);
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-Key", cbpKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    builder.toUriString(), HttpMethod.GET, entity, JsonNode.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("CBP Trade Statistics API 호출 실패: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * NTIA Spectrum Data API 호출
     */
    public Optional<JsonNode> callNtiaSpectrumData(String query) {
        try {
            String url = "https://www.ntia.gov/data/spectrum-map";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("search", query)
                    .queryParam("limit", 10);
            
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                    builder.toUriString(), JsonNode.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("NTIA Spectrum Data API 호출 실패: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * DOT Safety Data API 호출
     */
    public Optional<JsonNode> callDotSafetyData(String query) {
        try {
            String url = "https://www.nhtsa.gov/api";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("search", query)
                    .queryParam("limit", 10);
            
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                    builder.toUriString(), JsonNode.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("DOT Safety Data API 호출 실패: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * DOE Energy Data API 호출
     */
    public Optional<JsonNode> callDoeEnergyData(String query) {
        try {
            String url = "https://api.eia.gov/petroleum";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("search", query)
                    .queryParam("limit", 10);
            
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                    builder.toUriString(), JsonNode.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("DOE Energy Data API 호출 실패: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * DOI Natural Resources API 호출
     */
    public Optional<JsonNode> callDoiNaturalResources(String query) {
        try {
            String url = "https://data.doi.gov/Minerals";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("search", query)
                    .queryParam("limit", 10);
            
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                    builder.toUriString(), JsonNode.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("DOI Natural Resources API 호출 실패: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * DOL Employment Data API 호출
     */
    public Optional<JsonNode> callDolEmploymentData(String query) {
        try {
            String url = "https://api.dol.gov/unemployment";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("search", query)
                    .queryParam("limit", 10);
            
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                    builder.toUriString(), JsonNode.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("DOL Employment Data API 호출 실패: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Census International Trade HS API 호출
     * 대한민국 CTY_CODE=5800 사용
     */
    public Optional<JsonNode> callCensusInternationalTradeHs(String hsCode, String tradeType, String year, String month) {
        try {
            // tradeType: "imports" 또는 "exports"
            String url = "https://api.census.gov/data/timeseries/intltrade/" + tradeType + "/hs";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("get", "CTY_CODE,HS,COMM_LVL,GEN_VAL_MO")
                    .queryParam("time", year + "-" + month)
                    .queryParam("CTY_CODE", "5800")  // 대한민국
                    .queryParam("COMM_LVL", "HS6");
            
            // HS 코드 지정 (선택적)
            if (hsCode != null && !hsCode.isEmpty()) {
                builder.queryParam("HS", hsCode);
            }
            
            // Census API 키 추가
            if (censusKey != null && !censusKey.isEmpty()) {
                builder.queryParam("key", censusKey);
            }
            
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                    builder.toUriString(), JsonNode.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("Census International Trade HS API 호출 실패: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * API 엔드포인트 정보 조회
     * AI 엔진에서 사용하는 API 엔드포인트 정보 반환
     */
    public java.util.Map<String, Object> getApiEndpointsInfo() {
        java.util.Map<String, Object> endpointsInfo = new java.util.HashMap<>();
        java.util.List<String> agencies = java.util.Arrays.asList(
            "FDA", "EPA", "USDA", "CPSC", "FCC", "CBP", "Commerce"
        );
        
        endpointsInfo.put("agencies", agencies);
        endpointsInfo.put("total_endpoints", agencies.size());
        endpointsInfo.put("timestamp", java.time.Instant.now().toString());
        
        log.info("✅ API 엔드포인트 정보 반환: {}개 기관", agencies.size());
        return endpointsInfo;
    }
}
