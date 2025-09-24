package com.suracle.backend_api.service.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
            
            // openFDA cosmetics: correct base path is plural 'cosmetics'
            URI base = URI.create("https://api.fda.gov/cosmetics/event.json");
            // String[] tokens = name.isEmpty() ? new String[0] : name.split("\\s+");

            // AND token query first
            {
                String[] tokens = name.isEmpty() ? new String[0] : name.split("\\s+");
                String tokenExpr = String.join(" AND ", tokens);
                if (tokenExpr.isEmpty()) {
                    tokenExpr = name;
                }
                String searchQuery = "products.name_brand:(" + tokenExpr + ")";
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
            
            URI base = URI.create("https://api.fda.gov/food/enforcement.json");

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
            
            URI base = URI.create("https://api.fda.gov/food/event.json");
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
            String raw = deviceName == null ? "" : deviceName.trim();
            String[] tokens = raw.isEmpty() ? new String[0] : raw.split("\\s+");
            String spaceExpr = String.join(" ", tokens);
            String orExpr = String.join(" OR ", tokens);
            if (spaceExpr.isEmpty()) spaceExpr = raw;
            if (orExpr.isEmpty()) orExpr = raw;

            String[] strategies = new String[]{"and_tokens", "or_tokens"};
            String[] values = new String[]{spaceExpr, orExpr};

            for (int i = 0; i < strategies.length; i++) {
                String strat = strategies[i];
                String val = values[i].replace(' ', '+');
                URI uri = UriComponentsBuilder
                        .fromUriString("https://api.fcc.gov/device/authorization/grants")
                        .queryParam("search", "device_name:" + val)
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
                            JsonNode json = objectMapper.readTree(body);
                            if (json.isObject()) {
                                ObjectNode meta = ((ObjectNode) json).with("_meta");
                                meta.put("strategy", strat);
                                meta.put("query_used", "device_name:" + val);
                            }
                            return Optional.of(json);
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
            }
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
                URI uri = UriComponentsBuilder
                        .fromUriString("https://www.cpsc.gov/Recalls/CPSC-Recalls-API/recalls.json")
                        .queryParam("search", val)
                        .queryParam("limit", 10)
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
                URI uri = UriComponentsBuilder
                        .fromUriString("https://comptox.epa.gov/dashboard/api/chemical/search")
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
}


