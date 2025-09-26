package com.suracle.backend_api.service.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.suracle.backend_api.config.ApiEndpointsManager;
import com.suracle.backend_api.config.ApiKeysProperties;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequirementsApiClient {

    private final ApiEndpointsManager apiEndpointsManager;
    private final ApiKeysProperties apiKeysProperties;
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
            
            // ApiEndpointsManager를 사용하여 엔드포인트 가져오기
            URI base = URI.create(apiEndpointsManager.getEndpoint("fda", "cosmetic", "event"));

            // 단순한 검색어로 시도 (FDA API는 복잡한 쿼리를 지원하지 않을 수 있음)
            {
                String searchQuery = name.isEmpty() ? "cosmetic" : name;
                URI uri = UriComponentsBuilder.fromUri(base)
                        .queryParam("search", searchQuery)
                        .queryParam("limit", 10)
                        .build(true).toUri();
                log.info("📡 시도 1 (AND tokens): {}", uri);
                ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                String body = resp.getBody();
                log.info("📊 응답 상태: {} (Content-Length: {})", resp.getStatusCode(), body != null ? body.length() : 0);
                if (resp.getStatusCode().is2xxSuccessful()) {
                    if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                        JsonNode json = objectMapper.readTree(body);
                        if (json.isObject()) {
                            ObjectNode meta = ((ObjectNode) json).with("_meta");
                            meta.put("strategy", "and_tokens");
                            meta.put("query_used", searchQuery);
                        }
                        int resultCount = json.path("results").size();
                        log.info("✅ FDA Cosmetic Event(AND) 성공: {}개 결과", resultCount);
                        return Optional.of(json);
                    }
                }
            }
            // OR token query second
            {
                String[] tokens = name.isEmpty() ? new String[0] : name.split("\\s+");
                String tokenExpr = String.join(" OR ", tokens);
                if (tokenExpr.isEmpty()) {
                    tokenExpr = name;
                }
                String searchQuery = "products.name_brand:(" + tokenExpr + ")";
                URI uri = UriComponentsBuilder.fromUri(base)
                        .queryParam("search", searchQuery)
                        .queryParam("limit", 10)
                        .build(true).toUri();
                log.info("📡 시도 2 (OR tokens): {}", uri);
                ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                String body = resp.getBody();
                log.info("📊 응답 상태: {} (Content-Length: {})", resp.getStatusCode(), body != null ? body.length() : 0);
                if (resp.getStatusCode().is2xxSuccessful()) {
                    if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                        JsonNode json = objectMapper.readTree(body);
                        if (json.isObject()) {
                            ObjectNode meta = ((ObjectNode) json).with("_meta");
                            meta.put("strategy", "or_tokens");
                            meta.put("query_used", searchQuery);
                        }
                        int resultCount = json.path("results").size();
                        log.info("✅ FDA Cosmetic Event(OR) 성공: {}개 결과", resultCount);
                        return Optional.of(json);
                    }
                }
            }
            // exact phrase fallback third
            {
                String searchQuery = "products.name_brand:\"" + name + "\"";
                URI uri = UriComponentsBuilder.fromUri(base)
                        .queryParam("search", searchQuery)
                        .queryParam("limit", 10)
                        .build(true).toUri();
                log.info("📡 시도 3 (exact phrase): {}", uri);
                ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                String body = resp.getBody();
                log.info("📊 응답 상태: {} (Content-Length: {})", resp.getStatusCode(), body != null ? body.length() : 0);
                if (resp.getStatusCode().is2xxSuccessful()) {
                    if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                        JsonNode json = objectMapper.readTree(body);
                        if (json.isObject()) {
                            ObjectNode meta = ((ObjectNode) json).with("_meta");
                            meta.put("strategy", "exact_phrase");
                            meta.put("query_used", searchQuery);
                        }
                        int resultCount = json.path("results").size();
                        log.info("✅ FDA Cosmetic Event(exact) 성공: {}개 결과", resultCount);
                        return Optional.of(json);
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
            
            // ApiEndpointsManager를 사용하여 엔드포인트 가져오기
            URI base = URI.create(apiEndpointsManager.getEndpoint("fda", "food", "enforcement"));

            // 1) AND tokens
            {
                String[] tokens = name.isEmpty() ? new String[0] : name.split("\\s+");
                String tokenExpr = String.join(" AND ", tokens);
                if (tokenExpr.isEmpty()) tokenExpr = name;
                String searchQuery = "product_description:(" + tokenExpr + ")";
                URI uri = UriComponentsBuilder.fromUri(base)
                        .queryParam("search", searchQuery)
                        .queryParam("limit", 10)
                        .build(true).toUri();
                log.info("📡 시도 1 (AND tokens): {}", uri);
                ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                String body = resp.getBody();
                log.info("📊 응답 상태: {} (Content-Length: {})", resp.getStatusCode(), body != null ? body.length() : 0);
                if (resp.getStatusCode().is2xxSuccessful() && body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                    JsonNode json = objectMapper.readTree(body);
                    if (json.isObject()) {
                        ObjectNode meta = ((ObjectNode) json).with("_meta");
                        meta.put("strategy", "and_tokens");
                        meta.put("query_used", searchQuery);
                    }
                    int resultCount = json.path("results").size();
                    log.info("✅ FDA Food Enforcement(AND) 성공: {}개 결과", resultCount);
                    return Optional.of(json);
                }
            }

            // 2) OR tokens
            {
                String[] tokens = name.isEmpty() ? new String[0] : name.split("\\s+");
                String tokenExpr = String.join(" OR ", tokens);
                if (tokenExpr.isEmpty()) tokenExpr = name;
                String searchQuery = "product_description:(" + tokenExpr + ")";
                URI uri = UriComponentsBuilder.fromUri(base)
                        .queryParam("search", searchQuery)
                        .queryParam("limit", 10)
                        .build(true).toUri();
                log.info("📡 시도 2 (OR tokens): {}", uri);
                ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                String body = resp.getBody();
                log.info("📊 응답 상태: {} (Content-Length: {})", resp.getStatusCode(), body != null ? body.length() : 0);
                if (resp.getStatusCode().is2xxSuccessful() && body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                    JsonNode json = objectMapper.readTree(body);
                    if (json.isObject()) {
                        ObjectNode meta = ((ObjectNode) json).with("_meta");
                        meta.put("strategy", "or_tokens");
                        meta.put("query_used", searchQuery);
                    }
                    int resultCount = json.path("results").size();
                    log.info("✅ FDA Food Enforcement(OR) 성공: {}개 결과", resultCount);
                    return Optional.of(json);
                }
            }

            // 3) exact phrase
            {
                String searchQuery = "product_description:\"" + name + "\"";
                URI uri = UriComponentsBuilder.fromUri(base)
                        .queryParam("search", searchQuery)
                        .queryParam("limit", 10)
                        .build(true).toUri();
                log.info("📡 시도 3 (exact phrase): {}", uri);
                ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                String body = resp.getBody();
                log.info("📊 응답 상태: {} (Content-Length: {})", resp.getStatusCode(), body != null ? body.length() : 0);
                if (resp.getStatusCode().is2xxSuccessful() && body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                    JsonNode json = objectMapper.readTree(body);
                    if (json.isObject()) {
                        ObjectNode meta = ((ObjectNode) json).with("_meta");
                        meta.put("strategy", "exact_phrase");
                        meta.put("query_used", searchQuery);
                    }
                    int resultCount = json.path("results").size();
                    log.info("✅ FDA Food Enforcement(exact) 성공: {}개 결과", resultCount);
                    return Optional.of(json);
                }
            }

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
            
            // ApiEndpointsManager를 사용하여 엔드포인트 가져오기
            URI base = URI.create(apiEndpointsManager.getEndpoint("fda", "food", "event"));
            // 1) AND tokens
            {
                String[] tokens = name.isEmpty() ? new String[0] : name.split("\\s+");
                String tokenExpr = String.join(" AND ", tokens);
                if (tokenExpr.isEmpty()) tokenExpr = name;
                String searchQuery = "products.name_brand:(" + tokenExpr + ")";
                URI uri = UriComponentsBuilder.fromUri(base)
                        .queryParam("search", searchQuery)
                        .queryParam("limit", 10)
                        .build(true).toUri();
                log.info("📡 시도 1 (AND tokens): {}", uri);
                ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                String body = resp.getBody();
                log.info("📊 응답 상태: {} (Content-Length: {})", resp.getStatusCode(), body != null ? body.length() : 0);
                if (resp.getStatusCode().is2xxSuccessful() && body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                    JsonNode json = objectMapper.readTree(body);
                    if (json.isObject()) {
                        ObjectNode meta = ((ObjectNode) json).with("_meta");
                        meta.put("strategy", "and_tokens");
                        meta.put("query_used", searchQuery);
                    }
                    int resultCount = json.path("results").size();
                    log.info("✅ FDA Food Event(AND) 성공: {}개 결과", resultCount);
                    return Optional.of(json);
                }
            }

            // 2) OR tokens
            {
                String[] tokens = name.isEmpty() ? new String[0] : name.split("\\s+");
                String tokenExpr = String.join(" OR ", tokens);
                if (tokenExpr.isEmpty()) tokenExpr = name;
                String searchQuery = "products.name_brand:(" + tokenExpr + ")";
                URI uri = UriComponentsBuilder.fromUri(base)
                        .queryParam("search", searchQuery)
                        .queryParam("limit", 10)
                        .build(true).toUri();
                log.info("📡 시도 2 (OR tokens): {}", uri);
                ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                String body = resp.getBody();
                log.info("📊 응답 상태: {} (Content-Length: {})", resp.getStatusCode(), body != null ? body.length() : 0);
                if (resp.getStatusCode().is2xxSuccessful() && body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                    JsonNode json = objectMapper.readTree(body);
                    if (json.isObject()) {
                        ObjectNode meta = ((ObjectNode) json).with("_meta");
                        meta.put("strategy", "or_tokens");
                        meta.put("query_used", searchQuery);
                    }
                    int resultCount = json.path("results").size();
                    log.info("✅ FDA Food Event(OR) 성공: {}개 결과", resultCount);
                    return Optional.of(json);
                }
            }

            // 3) exact phrase
            {
                String searchQuery = "products.name_brand:\"" + name + "\"";
                URI uri = UriComponentsBuilder.fromUri(base)
                        .queryParam("search", searchQuery)
                        .queryParam("limit", 10)
                        .build(true).toUri();
                log.info("📡 시도 3 (exact phrase): {}", uri);
                ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                String body = resp.getBody();
                log.info("📊 응답 상태: {} (Content-Length: {})", resp.getStatusCode(), body != null ? body.length() : 0);
                if (resp.getStatusCode().is2xxSuccessful() && body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                    JsonNode json = objectMapper.readTree(body);
                    if (json.isObject()) {
                        ObjectNode meta = ((ObjectNode) json).with("_meta");
                        meta.put("strategy", "exact_phrase");
                        meta.put("query_used", searchQuery);
                    }
                    int resultCount = json.path("results").size();
                    log.info("✅ FDA Food Event(exact) 성공: {}개 결과", resultCount);
                    return Optional.of(json);
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
            // EPA SRS chemname 엔드포인트는 특별한 형태이므로 직접 구성
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
            // ApiEndpointsManager를 사용하여 엔드포인트 가져오기
            String endpointUrl = apiEndpointsManager.getEndpoint("usda", "fooddata_central", "search");
            UriComponentsBuilder b = UriComponentsBuilder
                    .fromUriString(endpointUrl)
                    .queryParam("query", englishName)
                    .queryParam("pageSize", 10)
                    .queryParam("pageNumber", 1);
            
            // API 키가 없으면 설정에서 가져오기
            String finalApiKey = apiKey != null && !apiKey.isBlank() ? apiKey : apiKeysProperties.getUsdaKey();
            if (finalApiKey != null && !finalApiKey.isBlank()) {
                b.queryParam("api_key", finalApiKey);
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
            log.info("🔍 FCC API 호출: '{}'", deviceName);
            
            // FCC Public Files API의 다양한 엔드포인트 시도
            String[] endpoints = {
                apiEndpointsManager.getEndpoint("fcc", "service_data", "facility_search"),
                apiEndpointsManager.getEndpoint("fcc", "public_files", "search"),
                apiEndpointsManager.getEndpoint("fcc", "public_files", "stations"),
                apiEndpointsManager.getEndpoint("fcc", "public_files", "file_history"),
                apiEndpointsManager.getEndpoint("fcc", "manager_api", "search_files_folders"),
                apiEndpointsManager.getEndpoint("fcc", "device_authorization", "grants")
            };
            
            for (String endpointUrl : endpoints) {
                try {
                    URI uri;
                    
                    // 엔드포인트에 {keyword} 또는 {searchKey}가 있으면 경로 파라미터로 처리
                    if (endpointUrl.contains("{keyword}")) {
                        uri = UriComponentsBuilder
                                .fromUriString(endpointUrl.replace("{keyword}", deviceName))
                                .build(true)
                                .toUri();
                    } else if (endpointUrl.contains("{searchKey}")) {
                        uri = UriComponentsBuilder
                                .fromUriString(endpointUrl.replace("{searchKey}", deviceName))
                                .build(true)
                                .toUri();
                    } else {
                        // 쿼리 파라미터로 처리
                        uri = UriComponentsBuilder
                                .fromUriString(endpointUrl)
                                .queryParam("q", deviceName)
                        .queryParam("limit", 10)
                        .queryParam("format", "json")
                        .build(true)
                        .toUri();
                    }

                    log.info("📡 FCC API 시도: {}", uri);
                    
                    ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                    if (resp.getStatusCode().is2xxSuccessful()) {
                        String body = resp.getBody();
                        if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                            JsonNode json = objectMapper.readTree(body);
                            int resultCount = json.isArray() ? json.size() : 1;
                            log.info("✅ FCC API 성공: {}개 결과", resultCount);
                            return Optional.of(json);
                        }
                    }
                } catch (Exception e) {
                    log.warn("⚠️ FCC API 엔드포인트 실패: {}", e.getMessage());
                        continue;
                }
            }
            
            log.warn("❌ FCC API 모든 엔드포인트 실패");
        } catch (Exception e) {
            log.warn("FCC grants call failed: {}", e.toString());
        }
        return Optional.empty();
    }

    public Optional<JsonNode> callCpscRecallsJson(String englishName) {
        try {
            String raw = englishName == null ? "" : englishName.trim();
            String[] tokens = raw.isEmpty() ? new String[0] : raw.split("\\s+");
            String spaceExpr = String.join(" ", tokens).replace(' ', '+');
            String orExpr = String.join("+OR+", tokens);
            if (spaceExpr.isEmpty()) spaceExpr = raw.replace(' ', '+');
            if (orExpr.isEmpty()) orExpr = raw.replace(' ', '+');

            String[][] tries = new String[][]{
                    {"and_tokens", spaceExpr},
                    {"or_tokens", orExpr}
            };

            for (String[] t : tries) {
                String strat = t[0];
                String val = t[1];
                // ApiEndpointsManager를 사용하여 엔드포인트 가져오기 (SaferProducts 우선)
                String endpointUrl = apiEndpointsManager.getEndpoint("cpsc", "saferproducts", "recalls");
                URI uri = UriComponentsBuilder
                        .fromUriString(endpointUrl)
                        .queryParam("format", "json")
                        .queryParam("RecallDescription", val)
                        .queryParam("resultsPerPage", 10)
                        .build(true)
                        .toUri();
                ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                if (resp.getStatusCode().is2xxSuccessful()) {
                    String body = resp.getBody();
                    if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                        JsonNode json = objectMapper.readTree(body);
                        if (json.isObject()) {
                            ObjectNode meta = ((ObjectNode) json).with("_meta");
                            meta.put("strategy", strat);
                            meta.put("query_used", val);
                        }
                        return Optional.of(json);
                    } else {
                        log.info("CPSC recalls returned non-JSON body (likely HTML), will try saferproducts.gov fallback");
                    }
                } else {
                    log.info("CPSC recalls JSON non-2xx: {}", resp.getStatusCode());
                }
            }

            // Fallback: SaferProducts.gov official JSON API (업데이트된 URL 사용)
            try {
                String saferProductsUrl = apiEndpointsManager.getEndpoint("cpsc", "saferproducts", "api");
                URI sp = UriComponentsBuilder
                        .fromUriString(saferProductsUrl)
                        .queryParam("format", "json")
                        .queryParam("RecallDescription", englishName == null ? "" : englishName)
                        .queryParam("resultsPerPage", 10)
                        .build(true)
                        .toUri();
                ResponseEntity<String> spResp = restTemplate.getForEntity(sp, String.class);
                if (spResp.getStatusCode().is2xxSuccessful()) {
                    String body = spResp.getBody();
                    if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                        JsonNode json = objectMapper.readTree(body);
                        if (json.isObject()) {
                            ObjectNode meta = ((ObjectNode) json).with("_meta");
                            meta.put("strategy", "saferproducts_fallback");
                            meta.put("query_used", englishName == null ? "" : englishName);
                        }
                        return Optional.of(json);
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
            log.info("🔍 CBP API 호출: HS Code '{}'", hsCode);
            
            // CBP AESDirect API와 Public Data Portal 모두 시도
            String[] endpoints = {
                apiEndpointsManager.getEndpoint("cbp", "aesdirect", "weblink_inquiry"),
                apiEndpointsManager.getEndpoint("cbp", "public_data_portal", "trade_stats"),
                apiEndpointsManager.getEndpoint("cbp", "trade_statistics", "hs_codes")
            };
            
            for (String endpointUrl : endpoints) {
        try {
            UriComponentsBuilder b = UriComponentsBuilder
                            .fromUriString(endpointUrl)
                    .queryParam("hs_code", hsCode)
                    .queryParam("limit", 10)
                    .queryParam("format", "json");
                    
                    // API 키가 없으면 설정에서 가져오기
                    String finalApiKey = apiKey != null && !apiKey.isBlank() ? apiKey : apiKeysProperties.getCbpKey();
                    if (finalApiKey != null && !finalApiKey.isBlank()) {
                        b.queryParam("api_key", finalApiKey);
                    }
                    
            URI uri = b.build(true).toUri();
                    log.info("📡 CBP API 시도: {}", uri);
                    
            ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                    
            if (resp.getStatusCode().is2xxSuccessful()) {
                String body = resp.getBody();
                if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                            JsonNode json = objectMapper.readTree(body);
                            int resultCount = json.isArray() ? json.size() : 1;
                            log.info("✅ CBP API 성공: {}개 결과", resultCount);
                            return Optional.of(json);
                        } else {
                            log.info("⚠️ CBP API 비-JSON 응답 (HTML 등), 다음 엔드포인트 시도");
                        }
                } else {
                        log.warn("⚠️ CBP API 엔드포인트 실패: {}", resp.getStatusCode());
                    }
                } catch (Exception e) {
                    log.warn("⚠️ CBP API 엔드포인트 실패: {}", e.getMessage());
                    continue;
                }
            }
            
            log.warn("❌ CBP API 모든 엔드포인트 실패");
        } catch (Exception e) {
            log.warn("❌ CBP API 호출 실패: {}", e.getMessage());
        }
        return Optional.empty();
    }

    // NTIA (National Telecommunications and Information Administration) API
    public Optional<JsonNode> callNtiaSpectrumData(String query) {
        try {
            log.info("🔍 NTIA NBAM API 호출: '{}'", query);
            
            String endpointUrl = apiEndpointsManager.getEndpoint("ntia", "nbam_api", "search");
            URI uri = UriComponentsBuilder
                    .fromUriString(endpointUrl)
                    .queryParam("q", query)
                    .queryParam("limit", 10)
                    .queryParam("format", "json")
                    .build(true)
                    .toUri();
            
            log.info("📡 NTIA NBAM API 시도: {}", uri);
            
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                int resultCount = json.isArray() ? json.size() : 1;
                log.info("✅ NTIA NBAM API 성공: {}개 결과", resultCount);
                return Optional.of(json);
            }
            
            log.warn("❌ NTIA NBAM API 응답 실패: {}", response.getStatusCode());
        } catch (Exception e) {
            log.warn("❌ NTIA NBAM API 호출 실패: {}", e.getMessage());
        }
        return Optional.empty();
    }

    // DOT (Department of Transportation) API
    public Optional<JsonNode> callDotSafetyData(String query) {
        try {
            log.info("🔍 DOT Safety Data API 호출: '{}'", query);
            
            String endpointUrl = apiEndpointsManager.getEndpoint("dot", "safety_data", "nhtsa");
            URI uri = UriComponentsBuilder
                    .fromUriString(endpointUrl)
                    .queryParam("q", query)
                    .queryParam("limit", 10)
                    .queryParam("format", "json")
                    .build(true)
                    .toUri();
            
            log.info("📡 DOT API 시도: {}", uri);
            
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                int resultCount = json.isArray() ? json.size() : 1;
                log.info("✅ DOT Safety Data 성공: {}개 결과", resultCount);
                return Optional.of(json);
            }
            
            log.warn("❌ DOT Safety Data 응답 실패: {}", response.getStatusCode());
        } catch (Exception e) {
            log.warn("❌ DOT Safety Data 호출 실패: {}", e.getMessage());
        }
        return Optional.empty();
    }

    // DOE (Department of Energy) API
    public Optional<JsonNode> callDoeEnergyData(String query) {
        try {
            log.info("🔍 DOE PAGES API 호출: '{}'", query);
            
            String endpointUrl = apiEndpointsManager.getEndpoint("doe", "pages_api", "records");
            URI uri = UriComponentsBuilder
                    .fromUriString(endpointUrl)
                    .queryParam("q", query)
                    .queryParam("rows", 10)
                    .queryParam("page", 1)
                    .build(true)
                    .toUri();
            
            log.info("📡 DOE PAGES API 시도: {}", uri);
            
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                int resultCount = json.isArray() ? json.size() : 1;
                log.info("✅ DOE PAGES API 성공: {}개 결과", resultCount);
                return Optional.of(json);
            }
            
            log.warn("❌ DOE PAGES API 응답 실패: {}", response.getStatusCode());
        } catch (Exception e) {
            log.warn("❌ DOE PAGES API 호출 실패: {}", e.getMessage());
        }
        return Optional.empty();
    }

    // DOI (Department of the Interior) API
    public Optional<JsonNode> callDoiNaturalResources(String query) {
        try {
            log.info("🔍 DOI Natural Resources API 호출: '{}'", query);
            
            String endpointUrl = apiEndpointsManager.getEndpoint("doi", "natural_resources", "minerals");
            URI uri = UriComponentsBuilder
                    .fromUriString(endpointUrl)
                    .queryParam("q", query)
                    .queryParam("limit", 10)
                    .queryParam("format", "json")
                    .build(true)
                    .toUri();
            
            log.info("📡 DOI API 시도: {}", uri);
            
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                int resultCount = json.isArray() ? json.size() : 1;
                log.info("✅ DOI Natural Resources 성공: {}개 결과", resultCount);
                return Optional.of(json);
            }
            
            log.warn("❌ DOI Natural Resources 응답 실패: {}", response.getStatusCode());
        } catch (Exception e) {
            log.warn("❌ DOI Natural Resources 호출 실패: {}", e.getMessage());
        }
        return Optional.empty();
    }

    // DOL (Department of Labor) API
    public Optional<JsonNode> callDolEmploymentData(String query) {
        try {
            log.info("🔍 DOL Data Portal API 호출: '{}'", query);
            
            String endpointUrl = apiEndpointsManager.getEndpoint("dol", "data_portal", "api");
            URI uri = UriComponentsBuilder
                    .fromUriString(endpointUrl)
                    .queryParam("q", query)
                    .queryParam("limit", 10)
                    .queryParam("format", "json")
                    .build(true)
                    .toUri();
            
            log.info("📡 DOL Data Portal API 시도: {}", uri);
            
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                int resultCount = json.isArray() ? json.size() : 1;
                log.info("✅ DOL Data Portal API 성공: {}개 결과", resultCount);
                return Optional.of(json);
            }
            
            log.warn("❌ DOL Data Portal API 응답 실패: {}", response.getStatusCode());
        } catch (Exception e) {
            log.warn("❌ DOL Data Portal API 호출 실패: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<JsonNode> callEpaCompToxSearch(String query) {
        try {
            String raw = (query == null ? "" : query).trim();
            String[] tokens = raw.isEmpty() ? new String[0] : raw.split("\\s+");
            String spaceExpr = String.join(" ", tokens).replace(' ', '+');
            String orExpr = String.join("+OR+", tokens);
            if (spaceExpr.isEmpty()) spaceExpr = raw.replace(' ', '+');
            if (orExpr.isEmpty()) orExpr = raw.replace(' ', '+');

            String[][] tries = new String[][]{
                    {"and_tokens", spaceExpr},
                    {"or_tokens", orExpr}
            };

            for (String[] t : tries) {
                String strat = t[0];
                String val = t[1];
                // ApiEndpointsManager를 사용하여 엔드포인트 가져오기
                String endpointUrl = apiEndpointsManager.getEndpoint("epa", "chemicals", "search");
                URI uri = UriComponentsBuilder
                        .fromUriString(endpointUrl)
                        .queryParam("search", val)
                        .queryParam("limit", 10)
                        .build(true)
                        .toUri();
                log.info("📡 EPA CompTox URL({}): {}", strat, uri);

                ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
                String body = resp.getBody();
                log.info("📊 EPA CompTox 응답 상태: {} (Content-Length: {})", resp.getStatusCode(), body != null ? body.length() : 0);

                if (resp.getStatusCode().is2xxSuccessful()) {
                    if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                        JsonNode json = objectMapper.readTree(body);
                        if (json.isObject()) {
                            ObjectNode meta = ((ObjectNode) json).with("_meta");
                            meta.put("strategy", strat);
                            meta.put("query_used", val);
                        }
                        int resultCount = json.path("results").size();
                        log.info("✅ EPA CompTox 성공: {}개 결과", resultCount);
                        return Optional.of(json);
                    }
                }
            }
        } catch (Exception e) {
            // log.warn("❌ EPA CompTox 호출 실패: {}", e.getMessage());
        }
        return Optional.empty();
    }
    
    /**
     * EPA Envirofacts Chemical Search API 호출
     */
    public Optional<JsonNode> callEpaEnvirofactsSearch(String query) {
        try {
            log.info("🔍 EPA Envirofacts Chemical Search API 호출: '{}'", query);
            
            // 여러 EPA 테이블에서 시도
            String[] endpoints = {
                apiEndpointsManager.getEndpoint("epa", "envirofacts", "chemical_search"),
                apiEndpointsManager.getEndpoint("epa", "envirofacts", "facility_search"),
                apiEndpointsManager.getEndpoint("epa", "envirofacts", "tri_search"),
                apiEndpointsManager.getEndpoint("epa", "envirofacts", "rcra_search")
            };
            
            for (String endpointUrl : endpoints) {
                try {
                    String finalUrl = endpointUrl.replace("{query}", query);
                    log.info("📡 EPA Envirofacts 시도: {}", finalUrl);
                    
                    ResponseEntity<String> response = restTemplate.getForEntity(finalUrl, String.class);
                    
                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        JsonNode json = objectMapper.readTree(response.getBody());
                        int resultCount = json.isArray() ? json.size() : 1;
                        log.info("✅ EPA Envirofacts 성공: {}개 결과", resultCount);
                        return Optional.of(json);
                    }
                } catch (Exception e) {
                    log.warn("⚠️ EPA Envirofacts 엔드포인트 실패: {}", e.getMessage());
                    continue;
                }
            }
            
            log.warn("❌ EPA Envirofacts 모든 엔드포인트 실패");
        } catch (Exception e) {
            log.warn("❌ EPA Envirofacts Chemical Search 호출 실패: {}", e.getMessage());
        }
        return Optional.empty();
    }
    
    // Tavily Search는 AI Engine (Python)에서 처리됩니다
    // Backend API에서는 정부 기관의 직접 API만 처리합니다
    
    /**
     * Commerce Aluminum Import Monitor API 호출
     */
    public Optional<JsonNode> callCommerceAluminumImportMonitor(String hsCode) {
        try {
            String endpointUrl = apiEndpointsManager.getEndpoint("commerce", "aluminum_import", "monitoring");
            UriComponentsBuilder b = UriComponentsBuilder
                    .fromUriString(endpointUrl)
                    .queryParam("hs_code", hsCode)
                    .queryParam("limit", 10)
                    .queryParam("format", "json");
            
            URI uri = b.build(true).toUri();
            ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                String body = resp.getBody();
                if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                    return Optional.of(objectMapper.readTree(body));
                }
            }
            log.info("Commerce Aluminum Import Monitor non-2xx: {}", resp.getStatusCode());
        } catch (Exception e) {
            log.warn("Commerce Aluminum Import Monitor call failed: {}", e.toString());
        }
        return Optional.empty();
    }
    
    /**
     * Commerce Steel Import Monitoring API 호출
     */
    public Optional<JsonNode> callCommerceSteelImportMonitoring(String hsCode) {
        try {
            String endpointUrl = apiEndpointsManager.getEndpoint("commerce", "steel_import", "monitoring");
            UriComponentsBuilder b = UriComponentsBuilder
                    .fromUriString(endpointUrl)
                    .queryParam("hs_code", hsCode)
                    .queryParam("limit", 10)
                    .queryParam("format", "json");
            
            URI uri = b.build(true).toUri();
            ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                String body = resp.getBody();
                if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                    return Optional.of(objectMapper.readTree(body));
                }
            }
            log.info("Commerce Steel Import Monitoring non-2xx: {}", resp.getStatusCode());
        } catch (Exception e) {
            log.warn("Commerce Steel Import Monitoring call failed: {}", e.toString());
        }
        return Optional.empty();
    }
    
    /**
     * CBP ACE Portal API 호출
     */
    public Optional<JsonNode> callCbpAcePortal(String hsCode) {
        try {
            String endpointUrl = apiEndpointsManager.getEndpoint("cbp", "ace_portal", "api");
            UriComponentsBuilder b = UriComponentsBuilder
                    .fromUriString(endpointUrl)
                    .queryParam("hs_code", hsCode)
                    .queryParam("limit", 10)
                    .queryParam("format", "json");
            
            // API 키가 필요하면 설정에서 가져오기
            String apiKey = apiKeysProperties.getCbpKey();
            if (apiKey != null && !apiKey.isBlank()) {
                b.queryParam("api_key", apiKey);
            }
            
            URI uri = b.build(true).toUri();
            ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                String body = resp.getBody();
                if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                    return Optional.of(objectMapper.readTree(body));
                }
            }
            log.info("CBP ACE Portal non-2xx: {}", resp.getStatusCode());
        } catch (Exception e) {
            log.warn("CBP ACE Portal call failed: {}", e.toString());
        }
        return Optional.empty();
    }
    
    /**
     * FCC EAS Equipment Authorization API 호출
     */
    public Optional<JsonNode> callFccEasEquipmentAuthorization(String deviceName) {
        try {
            String endpointUrl = apiEndpointsManager.getEndpoint("fcc", "eas_equipment", "base");
            UriComponentsBuilder b = UriComponentsBuilder
                    .fromUriString(endpointUrl)
                    .queryParam("search", deviceName)
                    .queryParam("limit", 10)
                    .queryParam("format", "json");
            
            URI uri = b.build(true).toUri();
            ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                String body = resp.getBody();
                if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                    return Optional.of(objectMapper.readTree(body));
                }
            }
            log.info("FCC EAS Equipment Authorization non-2xx: {}", resp.getStatusCode());
        } catch (Exception e) {
            log.warn("FCC EAS Equipment Authorization call failed: {}", e.toString());
        }
        return Optional.empty();
    }
    
    /**
     * API 엔드포인트 정보 조회 (디버깅/관리용)
     */
    public Map<String, Object> getApiEndpointsInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("agencies", apiEndpointsManager.getAllAgencies());
        
        Map<String, Object> agencyDetails = new HashMap<>();
        for (String agency : apiEndpointsManager.getAllAgencies()) {
            Map<String, Object> details = new HashMap<>();
            details.put("baseUrl", apiEndpointsManager.getBaseUrl(agency));
            details.put("apiKeyRequired", apiEndpointsManager.isApiKeyRequired(agency));
            details.put("rateLimit", apiEndpointsManager.getRateLimit(agency));
            details.put("endpoints", apiEndpointsManager.getAllEndpoints(agency));
            agencyDetails.put(agency, details);
        }
        info.put("agencyDetails", agencyDetails);
        
        return info;
    }
}


