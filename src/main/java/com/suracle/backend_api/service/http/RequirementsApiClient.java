package com.suracle.backend_api.service.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequirementsApiClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = createRestTemplate();

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(5000);
        rf.setReadTimeout(8000);
        RestTemplate rt = new RestTemplate(rf);
        ClientHttpRequestInterceptor ua = (request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.set(HttpHeaders.USER_AGENT, "LawGenie-Backend/1.0");
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            return execution.execute(request, body);
        };
        rt.getInterceptors().add(ua);
        return rt;
    }

    public Optional<JsonNode> callOpenFdaCosmeticEvent(String englishName) {
        try {
            String name = englishName == null ? "" : englishName.trim();
            log.info("🔍 FDA Cosmetic Event API 호출 시작: '{}'", name);
            
            URI base = URI.create("https://api.fda.gov/cosmetic/event.json");
            // String[] tokens = name.isEmpty() ? new String[0] : name.split("\\s+");

            // exact
            {
                String searchQuery = "products.name_brand:" + name;
                URI uri = UriComponentsBuilder.fromUri(base)
                        .queryParam("search", searchQuery)
                        .queryParam("limit", 10)
                        .build(true).toUri();
                log.info("📡 시도 1 (exact): {}", uri);
                ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                String body = resp.getBody();
                log.info("📊 응답 상태: {} (Content-Length: {})", resp.getStatusCode(), body != null ? body.length() : 0);
                if (resp.getStatusCode().is2xxSuccessful()) {
                    if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                        JsonNode json = objectMapper.readTree(body);
                        int resultCount = json.path("results").size();
                        log.info("✅ FDA Cosmetic Event 성공: {}개 결과", resultCount);
                        return Optional.of(json);
                    } else {
                        log.warn("⚠️ FDA Cosmetic Event: JSON이 아닌 응답 (HTML?)");
                    }
                }
            }
            
            // AND (temporarily disabled for cleaner logs)
            
            log.info("❌ FDA Cosmetic Event: 모든 검색 전략 실패");
        } catch (Exception e) {
            log.warn("❌ FDA Cosmetic Event 호출 실패: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<JsonNode> callOpenFdaFoodEnforcement(String englishName) {
        try {
            String name = englishName == null ? "" : englishName.trim();
            log.info("🔍 FDA Food Enforcement API 호출 시작: '{}'", name);
            
            URI base = URI.create("https://api.fda.gov/food/enforcement.json");
            // String[] tokens = name.isEmpty() ? new String[0] : name.split("\\s+");

            // exact
            {
                String searchQuery = "product_description:" + name;
                URI uri = UriComponentsBuilder.fromUri(base)
                        .queryParam("search", searchQuery)
                        .queryParam("limit", 10)
                        .build(true).toUri();
                log.info("📡 시도 1 (exact): {}", uri);
                ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                String body = resp.getBody();
                log.info("📊 응답 상태: {} (Content-Length: {})", resp.getStatusCode(), body != null ? body.length() : 0);
                if (resp.getStatusCode().is2xxSuccessful()) {
                    if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                        JsonNode json = objectMapper.readTree(body);
                        int resultCount = json.path("results").size();
                        log.info("✅ FDA Food Enforcement 성공: {}개 결과", resultCount);
                        return Optional.of(json);
                    } else {
                        log.warn("⚠️ FDA Food Enforcement: JSON이 아닌 응답 (HTML?)");
                    }
                }
            }
            
            // AND (temporarily disabled for cleaner logs)
            
            log.info("❌ FDA Food Enforcement: 모든 검색 전략 실패");
        } catch (Exception e) {
            log.warn("❌ FDA Food Enforcement 호출 실패: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<JsonNode> callOpenFdaFoodEvent(String englishName) {
        try {
            String name = englishName == null ? "" : englishName.trim();
            log.info("🔍 FDA Food Event API 호출 시작: '{}'", name);
            
            URI base = URI.create("https://api.fda.gov/food/event.json");
            // String[] tokens = name.isEmpty() ? new String[0] : name.split("\\s+");

            // exact
            {
                String searchQuery = "products.name_brand:" + name;
                URI uri = UriComponentsBuilder.fromUri(base)
                        .queryParam("search", searchQuery)
                        .queryParam("limit", 10)
                        .build(true).toUri();
                log.info("📡 시도 1 (exact): {}", uri);
                ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                String body = resp.getBody();
                log.info("📊 응답 상태: {} (Content-Length: {})", resp.getStatusCode(), body != null ? body.length() : 0);
                if (resp.getStatusCode().is2xxSuccessful()) {
                    if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                        JsonNode json = objectMapper.readTree(body);
                        int resultCount = json.path("results").size();
                        log.info("✅ FDA Food Event 성공: {}개 결과", resultCount);
                        return Optional.of(json);
                    } else {
                        log.warn("⚠️ FDA Food Event: JSON이 아닌 응답 (HTML?)");
                    }
                }
            }
            
            log.info("❌ FDA Food Event: 모든 검색 전략 실패");
        } catch (Exception e) {
            log.warn("❌ FDA Food Event 호출 실패: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<JsonNode> callEpaSrsChemname(String query) {
        try {
            String encoded = UriComponentsBuilder.fromPath("/")
                    .pathSegment(query)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString()
                    .substring(1);
            URI uri = URI.create("https://cdxapps.epa.gov/ords/srs/srs_api/chemname/" + encoded);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set(org.springframework.http.HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.set(org.springframework.http.HttpHeaders.USER_AGENT, "LawGenie-Backend/1.0");
            ResponseEntity<String> resp = restTemplate.exchange(
                    uri, HttpMethod.GET, new org.springframework.http.HttpEntity<>(headers), String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                String body = resp.getBody();
                if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                    return Optional.of(objectMapper.readTree(body));
                }
            }
            if (resp.getStatusCode().value() == 404) {
                log.info("EPA SRS: no data (404) for query={}", query);
            }
        } catch (RestClientException e) {
            log.warn("EPA SRS call error: {}", e.toString());
        } catch (Exception e) {
            log.warn("EPA SRS parse error: {}", e.toString());
        }
        return Optional.empty();
    }

    public boolean checkCbpPortalReachable() {
        try {
            URI uri = URI.create("https://www.cbp.gov/newsroom/stats/cbp-public-data-portal");
            ResponseEntity<Void> resp = restTemplate.getForEntity(uri, Void.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("CBP portal check failed: {}", e.toString());
            return false;
        }
    }

    public Optional<JsonNode> callUsdaFoodDataCentralSearch(String englishName, String apiKey) {
        try {
            UriComponentsBuilder b = UriComponentsBuilder
                    .fromUriString("https://api.nal.usda.gov/fdc/v1/foods/search")
                    .queryParam("query", englishName)
                    .queryParam("pageSize", 10)
                    .queryParam("pageNumber", 1);
            if (apiKey != null && !apiKey.isBlank()) {
                b.queryParam("api_key", apiKey);
            }
            URI uri = b.build(true).toUri();
            ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                return Optional.of(objectMapper.readTree(resp.getBody()));
            }
            log.info("USDA FoodData Central non-2xx: {}", resp.getStatusCode());
        } catch (Exception e) {
            log.warn("USDA FoodData Central search failed: {}", e.toString());
        }
        return Optional.empty();
    }

    public Optional<JsonNode> callFccDeviceAuthorizationGrants(String deviceName) {
        try {
            String q = (deviceName == null ? "" : deviceName.trim().replace(' ', '+'));
            URI uri = UriComponentsBuilder
                    .fromUriString("https://api.fcc.gov/device/authorization/grants")
                    .queryParam("search", "device_name:" + q)
                    .queryParam("limit", 10)
                    .queryParam("format", "json")
                    .build(true)
                    .toUri();

                    
            int maxRetries = 3;
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                if (resp.getStatusCode().is2xxSuccessful()) {
                    String body = resp.getBody();
                    if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                        return Optional.of(objectMapper.readTree(body));
                    } else {
                        log.info("FCC grants returned non-JSON body (likely HTML), ignoring");
                        break;
                    }
                }
                int code = resp.getStatusCode().value();
                if (code == 502 && attempt < maxRetries) {
                    long backoffMs = (long) Math.pow(2, attempt - 1) * 1000L;
                    log.info("FCC 502 received, retrying attempt {}/{} after {}ms", attempt, maxRetries, backoffMs);
                    try { Thread.sleep(backoffMs); } catch (InterruptedException ignored) {}
                    continue;
                } else {
                    log.info("FCC grants non-2xx: {}", resp.getStatusCode());
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("FCC grants call failed: {}", e.toString());
        }
        return Optional.empty();
    }

    public Optional<JsonNode> callCpscRecallsJson(String englishName) {
        try {
            String q = (englishName == null ? "" : englishName.trim().replace(' ', '+'));
            URI uri = UriComponentsBuilder
                    .fromUriString("https://www.cpsc.gov/Recalls/CPSC-Recalls-API/recalls.json")
                    .queryParam("search", q)
                    .queryParam("limit", 10)
                    .build(true)
                    .toUri();
            ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                String body = resp.getBody();
                if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                    return Optional.of(objectMapper.readTree(body));
                } else {
                    log.info("CPSC recalls returned non-JSON body (likely HTML), will try saferproducts.gov fallback");
                }
            } else {
                log.info("CPSC recalls JSON non-2xx: {}", resp.getStatusCode());
            }

            // Fallback: SaferProducts.gov official JSON API
            try {
                URI sp = UriComponentsBuilder
                        .fromUriString("https://www.saferproducts.gov/RestWebServices/Recall")
                        .queryParam("format", "json")
                        .queryParam("RecallDescription", englishName == null ? "" : englishName)
                        .queryParam("resultsPerPage", 10)
                        .build(true)
                        .toUri();
                ResponseEntity<String> spResp = restTemplate.getForEntity(sp, String.class);
                if (spResp.getStatusCode().is2xxSuccessful()) {
                    String body = spResp.getBody();
                    if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                        return Optional.of(objectMapper.readTree(body));
                    }
                }
                log.info("SaferProducts fallback non-2xx or non-JSON: {}", spResp.getStatusCode());
            } catch (Exception spErr) {
                log.warn("SaferProducts fallback failed: {}", spErr.toString());
            }
        } catch (Exception e) {
            log.warn("CPSC recalls JSON call failed: {}", e.toString());
        }
        return Optional.empty();
    }

    public Optional<JsonNode> callCbpTradeStatisticsHsCodes(String hsCode, String apiKey) {
        try {
            UriComponentsBuilder b = UriComponentsBuilder
                    .fromUriString("https://api.cbp.gov/trade/statistics/hs-codes")
                    .queryParam("hs_code", hsCode)
                    .queryParam("limit", 10)
                    .queryParam("format", "json");
            if (apiKey != null && !apiKey.isBlank()) {
                b.queryParam("api_key", apiKey);
            }
            URI uri = b.build(true).toUri();
            ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                String body = resp.getBody();
                if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                    return Optional.of(objectMapper.readTree(body));
                } else {
                    log.info("CBP trade statistics returned non-JSON body (likely HTML), ignoring");
                }
            }
            log.info("CBP trade statistics non-2xx: {}", resp.getStatusCode());
        } catch (Exception e) {
            log.warn("CBP trade statistics call failed: {}", e.toString());
        }
        return Optional.empty();
    }

    public Optional<JsonNode> callEpaCompToxSearch(String query) {
        try {
            String q = (query == null ? "" : query).trim().replace(' ', '+');
            // log.info("🔍 EPA CompTox API 호출 시작: '{}'", q);
            
            URI uri = UriComponentsBuilder
                    .fromUriString("https://comptox.epa.gov/dashboard/api/chemical/search")
                    .queryParam("search", q)
                    .queryParam("limit", 10)
                    .build(true)
                    .toUri();
            log.info("📡 EPA CompTox URL: {}", uri);
            
            ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
            String body = resp.getBody();
            log.info("📊 EPA CompTox 응답 상태: {} (Content-Length: {})", resp.getStatusCode(), body != null ? body.length() : 0);
            
            if (resp.getStatusCode().is2xxSuccessful()) {
                if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                    JsonNode json = objectMapper.readTree(body);
                    int resultCount = json.path("results").size();
                    log.info("✅ EPA CompTox 성공: {}개 결과", resultCount);
                    return Optional.of(json);
                } else {
                    // log.warn("⚠️ EPA CompTox: HTML 대시보드 응답 (무시)");
                }
            } else {
                // log.info("❌ EPA CompTox: HTTP {} 응답", resp.getStatusCode().value());
            }
        } catch (Exception e) {
            // log.warn("❌ EPA CompTox 호출 실패: {}", e.getMessage());
        }
        return Optional.empty();
    }
}


