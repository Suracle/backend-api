package com.suracle.backend_api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.suracle.backend_api.service.http.RequirementsApiClient;
import com.suracle.backend_api.config.ApiKeysProperties;
import com.suracle.backend_api.service.util.EnglishNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

 

@RestController
@RequestMapping("/api/requirements/probe")
@RequiredArgsConstructor
@Slf4j
public class RequirementsProbeController {

    private final RequirementsApiClient client;
    private final ApiKeysProperties keys;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value = "/all", produces = "application/json; charset=UTF-8")
    public ResponseEntity<JsonNode> probeAll(
            @RequestParam(required = false) String product,
            @RequestParam(required = false) String hs,
            @RequestParam(required = false) String usdaKey,
            @RequestParam(required = false) String cbpKey,
            @RequestParam(required = false, defaultValue = "false") boolean includePayloads,
            @RequestParam(required = false, defaultValue = "false") boolean debug,
            @RequestParam(required = false, defaultValue = "false") boolean save,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String outputDir
    ) {
        // Defensive re-decode in case the servlet container delivered ISO-8859-1 decoded text
        product = redecodeIfIso(product);
        hs = redecodeIfIso(hs);
        String english = extractCoreKeyword(product);
        ObjectNode root = objectMapper.createObjectNode();
        root.put("product", product == null ? "" : product);
        root.put("english", english);
        root.put("hs", hs == null ? "" : hs);

        ObjectNode results = objectMapper.createObjectNode();
        client.callOpenFdaCosmeticEvent(english).ifPresentOrElse(json -> {
            results.put("fda_cosmetic_event", true);
            if (includePayloads) {
                results.set("fda_cosmetic_event_payload", json);
            }
        }, () -> results.put("fda_cosmetic_event", false));
        client.callOpenFdaFoodEnforcement(english).ifPresentOrElse(json -> {
            results.put("fda_food_enforcement", true);
            if (includePayloads) {
                results.set("fda_food_enforcement_payload", json);
            }
        }, () -> results.put("fda_food_enforcement", false));
        client.callOpenFdaFoodEvent(english).ifPresentOrElse(json -> {
            results.put("fda_food_event", true);
            if (includePayloads) {
                results.set("fda_food_event_payload", json);
            }
        }, () -> results.put("fda_food_event", false));
        String effectiveUsda = (usdaKey != null && !usdaKey.isBlank()) ? usdaKey : keys.getUsdaKey();
        String effectiveCbp = (cbpKey != null && !cbpKey.isBlank()) ? cbpKey : keys.getCbpKey();

        client.callUsdaFoodDataCentralSearch(english, effectiveUsda).ifPresentOrElse(json -> {
            results.put("usda_fooddata_search", true);
            if (includePayloads) results.set("usda_fooddata_search_payload", json);
        }, () -> results.put("usda_fooddata_search", false));
        client.callFccDeviceAuthorizationGrants(english).ifPresentOrElse(json -> {
            results.put("fcc_grants", true);
            if (includePayloads) results.set("fcc_grants_payload", json);
        }, () -> results.put("fcc_grants", false));
        client.callCpscRecallsJson(english).ifPresentOrElse(json -> {
            results.put("cpsc_recalls_json", true);
            if (includePayloads) results.set("cpsc_recalls_json_payload", json);
        }, () -> results.put("cpsc_recalls_json", false));
        client.callCbpTradeStatisticsHsCodes(hs == null ? "" : hs, effectiveCbp).ifPresentOrElse(json -> {
            results.put("cbp_trade_hs_codes", true);
            if (includePayloads) results.set("cbp_trade_hs_codes_payload", json);
        }, () -> results.put("cbp_trade_hs_codes", false));
        client.callEpaSrsChemname(english).ifPresentOrElse(json -> {
            results.put("epa_srs_chemname", true);
            if (includePayloads) results.set("epa_srs_chemname_payload", json);
        }, () -> results.put("epa_srs_chemname", false));
        client.callEpaCompToxSearch(english).ifPresentOrElse(json -> {
            results.put("epa_comptox_search", true);
            if (includePayloads) results.set("epa_comptox_search_payload", json);
        }, () -> results.put("epa_comptox_search", false));

        root.set("results", results);

        if (debug) {
            ObjectNode dbg = objectMapper.createObjectNode();
            // attempted URLs (primary ones only to avoid duplication)
            ObjectNode attempts = objectMapper.createObjectNode();
            attempts.put("fda_cosmetic_event", "https://api.fda.gov/cosmetic/event.json?search=" + urlEncode("products.name_brand:\"" + english + "\"") + "&limit=10");
            attempts.put("fda_food_enforcement", "https://api.fda.gov/food/enforcement.json?search=" + urlEncode("product_description:\"" + english + "\"") + "&limit=10");
            attempts.put("fda_food_event", "https://api.fda.gov/food/event.json?search=" + urlEncode("products.name_brand:\"" + english + "\"") + "&limit=10");
            attempts.put("usda_fooddata_search", "https://api.nal.usda.gov/fdc/v1/foods/search?query=" + urlEncode(english) + "&pageSize=10&pageNumber=1" + (effectiveUsda == null || effectiveUsda.isBlank() ? "" : ("&api_key=" + mask(effectiveUsda))));
            attempts.put("fcc_grants", "https://api.fcc.gov/device/authorization/grants?search=" + urlEncode("device_name:" + english) + "&limit=10&format=json");
            attempts.put("cpsc_recalls_json", "https://www.cpsc.gov/Recalls/CPSC-Recalls-API/recalls.json?search=" + urlEncode(english) + "&limit=10");
            attempts.put("cbp_trade_hs_codes", "https://api.cbp.gov/trade/statistics/hs-codes?hs_code=" + urlEncode(hs == null ? "" : hs) + "&limit=10&format=json" + (effectiveCbp == null || effectiveCbp.isBlank() ? "" : ("&api_key=" + mask(effectiveCbp))));
            attempts.put("epa_srs_chemname", "https://cdxapps.epa.gov/ords/srs/srs_api/chemname/" + urlEncode(english));
            attempts.put("epa_comptox_search", "https://comptox.epa.gov/dashboard/api/chemical/search?search=" + urlEncode(english) + "&limit=10");

            dbg.set("attempted_urls", attempts);

            // simple connectivity checks to hosts
            ObjectNode connectivity = objectMapper.createObjectNode();
            connectivity.set("api.fda.gov", head("https://api.fda.gov"));
            connectivity.set("api.nal.usda.gov", head("https://api.nal.usda.gov"));
            connectivity.set("comptox.epa.gov", head("https://comptox.epa.gov"));
            connectivity.set("cdxapps.epa.gov", head("https://cdxapps.epa.gov"));
            connectivity.set("api.fcc.gov", head("https://api.fcc.gov"));
            connectivity.set("www.cpsc.gov", head("https://www.cpsc.gov"));
            connectivity.set("api.cbp.gov", head("https://api.cbp.gov"));
            dbg.set("connectivity", connectivity);

            root.set("debug", dbg);
        }

        if (save) {
            try {
                String dir = (outputDir != null && !outputDir.isBlank()) ? outputDir : "requirements";
                java.nio.file.Path base = java.nio.file.Paths.get("src", "main", "resources", dir);
                java.nio.file.Files.createDirectories(base);
                String fileBase = (productId != null ? ("product-" + productId) : (english == null || english.isBlank() ? "product" : english.replaceAll("[^a-zA-Z0-9_-]", "_")));
                java.nio.file.Path out = base.resolve(fileBase + ".json");
                byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(root);
                java.nio.file.Files.write(out, bytes);
                root.put("saved", true);
                root.put("saved_path", out.toString());
            } catch (Exception e) {
                log.warn("Failed to save probe JSON", e);
                root.put("saved", false);
                root.put("save_error", e.getMessage());
            }
        }

        return ResponseEntity.ok(root);
    }

    private String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s == null ? "" : s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return s;
        }
    }

    private String mask(String s) {
        if (s == null || s.length() < 6) return "***";
        return s.substring(0, 3) + "***" + s.substring(s.length() - 3);
    }

    /**
     * 제품명에서 핵심 키워드만 추출하여 API 검색에 최적화된 영어 검색어 생성
     */
    private String extractCoreKeyword(String productName) {
        if (productName == null || productName.isBlank()) {
            return "";
        }
        
        String lower = productName.toLowerCase().trim();
        
        // 1. 한글 핵심 키워드 매핑 (우선순위 높음)
        if (lower.contains("세럼")) return "serum";
        if (lower.contains("크림")) return "cream";
        if (lower.contains("에센스")) return "essence";
        if (lower.contains("로션")) return "lotion";
        if (lower.contains("토너")) return "toner";
        if (lower.contains("마스크")) return "mask";
        if (lower.contains("스크럽")) return "scrub";
        if (lower.contains("클렌저")) return "cleanser";
        if (lower.contains("선크림") || lower.contains("자외선차단")) return "sunscreen";
        if (lower.contains("향수")) return "perfume";
        if (lower.contains("립밤")) return "lip balm";
        if (lower.contains("립스틱")) return "lipstick";
        
        // 비타민/성분 관련
        if (lower.contains("비타민c") || lower.contains("vitamin c")) return "vitamin c";
        if (lower.contains("비타민")) return "vitamin";
        if (lower.contains("콜라겐")) return "collagen";
        if (lower.contains("히알루론산")) return "hyaluronic acid";
        if (lower.contains("레티놀")) return "retinol";
        if (lower.contains("나이아신아마이드")) return "niacinamide";
        if (lower.contains("달팽이")) return "snail";
        if (lower.contains("진세노사이드")) return "ginsenoside";
        if (lower.contains("홍삼") || lower.contains("인삼")) return "ginseng";
        
        // 식품 관련
        if (lower.contains("김치")) return "kimchi";
        if (lower.contains("라면") || lower.contains("라멘")) return "noodles";
        if (lower.contains("밥") || lower.contains("쌀")) return "rice";
        if (lower.contains("면")) return "noodles";
        if (lower.contains("과자") || lower.contains("스낵")) return "snack";
        if (lower.contains("소스") || lower.contains("양념")) return "sauce";
        if (lower.contains("차") || lower.contains("티")) return "tea";
        
        // 전자제품
        if (lower.contains("노트북") || lower.contains("랩탑")) return "laptop";
        if (lower.contains("컴퓨터")) return "computer";
        if (lower.contains("스마트폰") || lower.contains("폰")) return "smartphone";
        if (lower.contains("태블릿")) return "tablet";
        
        // 2. 영어가 포함된 경우 첫 번째 영어 단어 추출
        String[] words = lower.split("\\s+");
        for (String word : words) {
            // 영어 단어만 추출 (한글 제외)
            if (word.matches("^[a-zA-Z]+$") && word.length() >= 2) {
                return word;
            }
        }
        
        // 3. 한글이 포함된 경우 첫 번째 한글 단어를 영어로 변환
        for (String word : words) {
            if (word.matches(".*[가-힣].*")) {
                // 한글 단어를 영어로 변환
                return EnglishNameUtil.toEnglishQuery(word);
            }
        }
        
        // 4. 마지막 수단: 전체를 영어로 변환하되 첫 단어만 사용
        String english = EnglishNameUtil.toEnglishQuery(productName);
        if (!english.isBlank() && !english.equals("product")) {
            return english.split("\\s+")[0]; // 첫 번째 단어만 반환
        }
        
        return "product";
    }

    private ObjectNode head(String url) {
        ObjectNode node = objectMapper.createObjectNode();
        try {
            java.net.http.HttpClient http = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(5))
                    .build();
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder(java.net.URI.create(url))
                    .method("HEAD", java.net.http.HttpRequest.BodyPublishers.noBody())
                    .timeout(java.time.Duration.ofSeconds(8))
                    .header("User-Agent", "LawGenie-Backend/1.0")
                    .build();
            java.net.http.HttpResponse<Void> resp = http.send(req, java.net.http.HttpResponse.BodyHandlers.discarding());
            node.put("status", resp.statusCode());
            node.put("ok", resp.statusCode() >= 200 && resp.statusCode() < 400);
        } catch (Exception e) {
            node.put("ok", false);
            node.put("error", e.getClass().getSimpleName());
            node.put("message", e.getMessage() == null ? "" : e.getMessage());
        }
        return node;
    }

    private String redecodeIfIso(String s) {
        if (s == null || s.isEmpty()) return s;
        // Heuristic: if contains replacement chars or seems mojibake, try re-encoding
        if (s.contains("��")) {
            try {
                byte[] iso = s.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
                return new String(iso, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ignored) {}
        }
        return s;
    }

}


