package com.suracle.backend_api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.suracle.backend_api.service.http.RequirementsApiClient;
import com.suracle.backend_api.service.util.ChemicalNameMapper;
import com.suracle.backend_api.service.util.EnglishNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.*;

/**
 * 요건 수집 전용 컨트롤러 (AI Engine Helper)
 * 
 * <p>이 컨트롤러는 AI Engine에서 호출하여 정부 API 데이터를 수집하고 정규화된 형태로 반환합니다.
 * 
 * <p>주요 기능:
 * <ul>
 *   <li>GET /api/requirements/collect: 정부 API 데이터 수집 및 정규화</li>
 *   <li>키워드 추출 (AI Engine API 호출 → fallback 휴리스틱)</li>
 *   <li>다중 정부 API 호출 (FDA, USDA, EPA, Census)</li>
 *   <li>OR 쿼리 지원 (여러 키워드 동시 검색)</li>
 *   <li>Citations 생성 (출처 URL 포함)</li>
 * </ul>
 * 
 * <p>호출 API:
 * <ul>
 *   <li>FDA Food Enforcement: /food/enforcement.json</li>
 *   <li>USDA FoodData Central: /fdc/v1/foods/search</li>
 *   <li>EPA CompTox: /dashboard/api/chemical/search</li>
 *   <li>Census International Trade: /data/timeseries/intltrade/imports/hs</li>
 * </ul>
 * 
 * <p>주의: 이 컨트롤러는 직접 사용자가 호출하지 않고 AI Engine에서만 사용합니다.
 * 
 * @see RequirementsApiClient
 */
@RestController
@RequestMapping("/api/requirements/collect")
@RequiredArgsConstructor
@Slf4j
public class RequirementsCollectorController {

    private final RequirementsApiClient client;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${ai.requirements-analysis.url:http://localhost:8000}")
    private String aiEngineUrl;

    /**
     * 요건 수집 메인 엔드포인트
     * 
     * @param product 제품명 (한글/영문)
     * @param hs HS 코드
     * @param includeRawData 원본 데이터 포함 여부
     * @return 정규화된 요건 데이터 + citations
     */
    @GetMapping(value = "", produces = "application/json; charset=UTF-8")
    public ResponseEntity<JsonNode> collectRequirements(
            @RequestParam(required = false) String product,
            @RequestParam(required = false) String hs,
            @RequestParam(required = false, defaultValue = "false") boolean includeRawData
    ) {
        try {
            log.info("📥 요건 수집 요청 - product: {}, hs: {}", product, hs);

            // 1. 키워드 정규화 및 추출 (AI Engine 사용)
            String normalizedKeyword = normalizeProductKeyword(product);
            List<String> keywords = extractKeywordsWithAi(product);
            String chemicalName = ChemicalNameMapper.toChemicalName(normalizedKeyword);
            
            System.out.println("🔄 정규화 완료:");
            System.out.println("  - keyword: " + normalizedKeyword);
            System.out.println("  - keywords (AI): " + keywords);
            System.out.println("  - keywords.size(): " + keywords.size());
            System.out.println("  - chemical: " + chemicalName);
            
            log.info("🔄 정규화 완료 - keyword: {}, keywords: {}, chemical: {}", 
                    normalizedKeyword, keywords, chemicalName);

            // 2. 데이터 수집 (OR 쿼리 사용)
            CollectedData collectedData = collectFromApis(normalizedKeyword, keywords, chemicalName, hs);

            // 3. 요건 추출
            ExtractedRequirements extracted = extractRequirements(collectedData);

            // 4. 응답 구성
            ObjectNode response = objectMapper.createObjectNode();
            response.put("product", product);
            response.put("normalized_keyword", normalizedKeyword);
            response.put("chemical_name", chemicalName);
            response.put("hs_code", hs);
            response.put("timestamp", java.time.Instant.now().toString());

            // 요건 데이터
            response.set("requirements", buildRequirementsNode(extracted));
            
            // 출처 정보 (citations)
            response.set("citations", buildCitationsNode(collectedData));

            // 원본 데이터 (선택)
            if (includeRawData) {
                response.set("raw_data", buildRawDataNode(collectedData));
            }

            log.info("✅ 요건 수집 완료 - total: {}, certifications: {}, documents: {}", 
                    extracted.getTotalCount(), 
                    extracted.getCertifications().size(),
                    extracted.getDocuments().size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 요건 수집 실패: {}", e.getMessage(), e);
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 제품 키워드 정규화
     */
    private String normalizeProductKeyword(String product) {
        if (product == null || product.isBlank()) {
            return "";
        }
        
        // 한글 → 영문 변환
        String english = EnglishNameUtil.toEnglishQuery(product);
        
        // 소문자 변환 및 공백 정리
        return english.toLowerCase().trim();
    }
    
    /**
     * AI Engine을 사용하여 키워드 추출
     * 
     * 우선순위:
     * 1. AI Engine API 호출 (OpenAI/HF)
     * 2. 로컬 휴리스틱 (폴백)
     */
    private List<String> extractKeywordsWithAi(String product) {
        try {
            // AI Engine API 호출
            String url = aiEngineUrl + "/requirements/extract-keywords";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("product_name", product);
            requestBody.put("product_description", "");
            requestBody.put("top_k", 5);
            requestBody.put("method", "auto");  // OpenAI → HF → Heuristic
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.debug("📡 AI Engine 키워드 추출 호출: {}", url);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    url,
                    entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                @SuppressWarnings("unchecked")
                List<String> keywords = (List<String>) result.get("keywords");
                String method = (String) result.get("method_used");
                
                log.info("✅ AI 키워드 추출 성공 - 방법: {}, 키워드: {}", method, keywords);
                
                return keywords != null ? keywords : List.of();
            }
            
        } catch (Exception e) {
            log.warn("⚠️ AI 키워드 추출 실패, 로컬 휴리스틱 사용: {}", e.getMessage());
        }
        
        // 폴백: 로컬 휴리스틱 키워드 추출
        return extractKeywords(product);
    }

    /**
     * 제품명에서 핵심 키워드 추출 (공백 기준 분리 + 불용어 제거)
     */
    private List<String> extractKeywords(String product) {
        if (product == null || product.isBlank()) {
            return List.of();
        }
        
        // 정규화된 키워드
        String normalized = normalizeProductKeyword(product);
        
        // 공백 기준 분리
        String[] tokens = normalized.split("\\s+");
        
        // 불용어 제거 및 필터링
        Set<String> stopWords = Set.of("the", "a", "an", "and", "or", "for", "with", "of", "in", "to", "on", "by");
        List<String> keywords = new ArrayList<>();
        
        for (String token : tokens) {
            if (!token.isBlank() && token.length() >= 3 && !stopWords.contains(token)) {
                keywords.add(token);
            }
        }
        
        // 카테고리 키워드 우선순위 부여
        // HS 코드 첫 2자리로 카테고리 판단
        List<String> priorityKeywords = new ArrayList<>();
        List<String> normalKeywords = new ArrayList<>();
        
        // HS 코드 기반 카테고리 키워드 (동적 생성)
        Set<String> categoryKeywords = getCategoryKeywordsByHsPrefix(
            product != null && product.length() >= 2 ? product.substring(0, 2) : ""
        );
        
        for (String kw : keywords) {
            if (categoryKeywords.contains(kw)) {
                priorityKeywords.add(kw);
            } else {
                normalKeywords.add(kw);
            }
        }
        
        // 우선순위 키워드를 앞에 배치
        List<String> result = new ArrayList<>();
        result.addAll(priorityKeywords);
        result.addAll(normalKeywords);
        
        // 최대 5개까지만
        return result.stream().limit(5).toList();
    }
    
    /**
     * HS 코드 접두사 기반 카테고리 키워드 반환
     */
    private Set<String> getCategoryKeywordsByHsPrefix(String hsPrefix) {
        // HS 코드 2자리 기반 카테고리 매핑
        Map<String, Set<String>> hsCategoryMap = Map.of(
            "33", Set.of("serum", "cream", "cosmetic", "skincare", "lotion", "mask", "perfume"),
            "21", Set.of("food", "beverage", "supplement", "vitamin", "extract", "ginseng"),
            "19", Set.of("food", "rice", "noodle", "pasta", "cereal"),
            "20", Set.of("food", "kimchi", "vegetable", "preserved"),
            "84", Set.of("device", "equipment", "machine", "computer", "laptop"),
            "85", Set.of("device", "equipment", "electronic", "telephone", "wireless"),
            "95", Set.of("toy", "game", "play", "children")
        );
        
        // 기본 카테고리 키워드
        Set<String> defaultKeywords = Set.of(
            "serum", "cream", "cosmetic", "food", "device", "equipment"
        );
        
        return hsCategoryMap.getOrDefault(hsPrefix, defaultKeywords);
    }

    /**
     * 정부 API에서 데이터 수집 (OR 쿼리 사용)
     */
    private CollectedData collectFromApis(String keyword, List<String> keywords, String chemicalName, String hsCode) {
        CollectedData data = new CollectedData();

        // FDA Food Enforcement (OR 쿼리 우선, 실패 시 단일 키워드)
        if (keywords != null && !keywords.isEmpty()) {
            client.callOpenFdaFoodEnforcementWithKeywords(keywords).ifPresentOrElse(
                json -> {
                    data.fdaFoodEnforcement = json;
                    String keywordsStr = String.join(" OR ", keywords);
                    data.citations.add(new Citation(
                        "FDA",
                        "Food Enforcement",
                        "https://api.fda.gov/food/enforcement.json?search=" + keywordsStr,
                        "FDA Food Enforcement Reports (OR query)"
                    ));
                    log.info("✅ FDA OR 쿼리 성공: {} 키워드", keywords.size());
                },
                () -> {
                    // OR 쿼리 실패 시 단일 키워드로 재시도
                    log.warn("⚠️ FDA OR 쿼리 실패, 단일 키워드로 재시도");
                    client.callOpenFdaFoodEnforcement(keyword).ifPresent(json -> {
                        data.fdaFoodEnforcement = json;
                        data.citations.add(new Citation(
                            "FDA",
                            "Food Enforcement",
                            "https://api.fda.gov/food/enforcement.json?search=" + keyword,
                            "FDA Food Enforcement Reports"
                        ));
                    });
                }
            );
        } else {
            // 키워드 없으면 단일 검색
            client.callOpenFdaFoodEnforcement(keyword).ifPresent(json -> {
                data.fdaFoodEnforcement = json;
                data.citations.add(new Citation(
                    "FDA",
                    "Food Enforcement",
                    "https://api.fda.gov/food/enforcement.json?search=" + keyword,
                    "FDA Food Enforcement Reports"
                ));
            });
        }

        // USDA FoodData Central (공백으로 자동 OR 처리됨)
        String usdaQuery = keywords != null && !keywords.isEmpty() 
                ? String.join(" ", keywords) 
                : keyword;
        
        client.callUsdaFoodDataCentralSearch(usdaQuery, null).ifPresent(json -> {
            data.usdaFdc = json;
            data.citations.add(new Citation(
                "USDA",
                "FoodData Central",
                "https://api.nal.usda.gov/fdc/v1/foods/search?query=" + usdaQuery,
                "USDA FoodData Central Database"
            ));
            log.info("✅ USDA 검색 성공: {}", usdaQuery);
        });

        // EPA CompTox (화학물질 - 정식명 우선)
        if (chemicalName != null && !chemicalName.isBlank()) {
            client.callEpaCompToxSearch(chemicalName).ifPresent(json -> {
                data.epaCompTox = json;
                data.citations.add(new Citation(
                    "EPA",
                    "CompTox Dashboard",
                    "https://comptox.epa.gov/dashboard/api/chemical/search?search=" + chemicalName,
                    "EPA CompTox Chemicals Dashboard"
                ));
                log.info("✅ EPA CompTox 검색 성공: {}", chemicalName);
            });
        }

        // Census International Trade (HS 코드)
        if (hsCode != null && !hsCode.isBlank()) {
            YearMonth latestMonth = YearMonth.now();
            String year = String.valueOf(latestMonth.getYear());
            String month = String.format("%02d", latestMonth.getMonthValue());

            client.callCensusInternationalTradeHs(hsCode, "imports", year, month).ifPresent(json -> {
                data.censusImports = json;
                data.citations.add(new Citation(
                    "Census",
                    "International Trade HS",
                    String.format("https://api.census.gov/data/timeseries/intltrade/imports/hs?HS=%s&time=%s-%s", 
                            hsCode, year, month),
                    "Census Bureau International Trade Statistics"
                ));
                log.info("✅ Census HS 검색 성공: {}", hsCode);
            });
        }

        return data;
    }

    /**
     * 수집된 데이터에서 요건 추출
     */
    private ExtractedRequirements extractRequirements(CollectedData data) {
        ExtractedRequirements requirements = new ExtractedRequirements();

        // FDA Food Enforcement에서 추출
        if (data.fdaFoodEnforcement != null) {
            extractFromFdaFoodEnforcement(data.fdaFoodEnforcement, requirements);
        }

        // USDA FDC에서 추출
        if (data.usdaFdc != null) {
            extractFromUsdaFdc(data.usdaFdc, requirements);
        }

        // EPA CompTox에서 추출
        if (data.epaCompTox != null) {
            extractFromEpaCompTox(data.epaCompTox, requirements);
        }

        // Census에서 추출
        if (data.censusImports != null) {
            extractFromCensus(data.censusImports, requirements);
        }

        return requirements;
    }

    /**
     * FDA Food Enforcement 데이터에서 요건 추출
     */
    private void extractFromFdaFoodEnforcement(JsonNode data, ExtractedRequirements requirements) {
        if (!data.has("results")) return;

        for (JsonNode result : data.get("results")) {
            String classification = result.path("classification").asText("");
            String reason = result.path("reason_for_recall").asText("");
            String reportDate = result.path("report_date").asText("");
            String recallDate = result.path("recall_initiation_date").asText("");

            if (containsRequirementKeywords(reason) || containsRequirementKeywords(classification)) {
                RequirementItem item = new RequirementItem();
                item.agency = "FDA";
                item.category = "enforcement";
                item.title = "Food Enforcement: " + classification;
                item.description = reason;
                item.source = "FDA Food Enforcement Reports";
                item.confidence = 0.8;
                item.keywords = extractKeywords(reason + " " + classification);
                
                // 날짜 정보 추가 (YYYYMMDD → YYYY-MM-DD)
                if (!recallDate.isEmpty()) {
                    item.effectiveDate = formatFdaDate(recallDate);
                } else if (!reportDate.isEmpty()) {
                    item.effectiveDate = formatFdaDate(reportDate);
                }

                requirements.addItem(item);
            }
        }
    }

    /**
     * FDA 날짜 형식 변환 (YYYYMMDD → YYYY-MM-DD)
     */
    private String formatFdaDate(String fdaDate) {
        if (fdaDate == null || fdaDate.length() != 8) {
            return "";
        }
        try {
            return fdaDate.substring(0, 4) + "-" + fdaDate.substring(4, 6) + "-" + fdaDate.substring(6, 8);
        } catch (Exception e) {
            return fdaDate;
        }
    }

    /**
     * USDA FDC 데이터에서 요건 추출
     */
    private void extractFromUsdaFdc(JsonNode data, ExtractedRequirements requirements) {
        if (!data.has("foods")) return;

        for (JsonNode food : data.get("foods")) {
            String description = food.path("description").asText("");
            String dataType = food.path("dataType").asText("");

            RequirementItem item = new RequirementItem();
            item.agency = "USDA";
            item.category = "food_data";
            item.title = "Food Database Entry: " + description;
            item.description = "Data Type: " + dataType;
            item.source = "USDA FoodData Central";
            item.confidence = 0.6;
            item.keywords = extractKeywords(description);

            requirements.addItem(item);
        }
    }

    /**
     * EPA CompTox 데이터에서 요건 추출
     */
    private void extractFromEpaCompTox(JsonNode data, ExtractedRequirements requirements) {
        if (data.isArray()) {
            for (JsonNode chemical : data) {
                String name = chemical.path("preferredName").asText("");
                String casrn = chemical.path("casrn").asText("");

                RequirementItem item = new RequirementItem();
                item.agency = "EPA";
                item.category = "chemical_safety";
                item.title = "Chemical: " + name;
                item.description = "CASRN: " + casrn;
                item.source = "EPA CompTox Dashboard";
                item.confidence = 0.7;
                item.keywords = List.of(name, casrn);

                requirements.addItem(item);
            }
        }
    }

    /**
     * Census 데이터에서 요건 추출
     */
    private void extractFromCensus(JsonNode data, ExtractedRequirements requirements) {
        if (!data.isArray() || data.size() < 2) return;

        RequirementItem item = new RequirementItem();
        item.agency = "Census";
        item.category = "trade_statistics";
        item.title = "International Trade Statistics";
        item.description = "HS Code import/export data available";
        item.source = "Census Bureau International Trade";
        item.confidence = 0.5;
        item.keywords = List.of("trade", "import", "export", "hs code");

        requirements.addItem(item);
    }

    /**
     * 요건 관련 키워드 포함 여부 확인
     */
    private boolean containsRequirementKeywords(String text) {
        if (text == null || text.isBlank()) return false;
        
        String lower = text.toLowerCase();
        String[] keywords = {
            "enforcement", "import", "labeling", "compliance", "certificate", 
            "prior notice", "cgmp", "regulation", "standard", "requirement",
            "prohibited", "restricted", "violation", "recall"
        };

        for (String keyword : keywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 요건 노드 구성
     */
    private ObjectNode buildRequirementsNode(ExtractedRequirements extracted) {
        ObjectNode node = objectMapper.createObjectNode();
        
        node.put("total_count", extracted.getTotalCount());
        node.set("certifications", buildItemsArray(extracted.getCertifications()));
        node.set("documents", buildItemsArray(extracted.getDocuments()));
        node.set("notices", buildItemsArray(extracted.getNotices()));
        node.set("all_items", buildItemsArray(extracted.getAllItems()));

        // 카테고리별 통계
        ObjectNode categoryStats = objectMapper.createObjectNode();
        Map<String, Long> stats = extracted.getCategoryStats();
        stats.forEach(categoryStats::put);
        node.set("category_stats", categoryStats);

        return node;
    }

    /**
     * 요건 아이템 배열 구성
     */
    private ArrayNode buildItemsArray(List<RequirementItem> items) {
        ArrayNode array = objectMapper.createArrayNode();
        
        for (RequirementItem item : items) {
            ObjectNode itemNode = objectMapper.createObjectNode();
            itemNode.put("agency", item.agency);
            itemNode.put("category", item.category);
            itemNode.put("title", item.title);
            itemNode.put("description", item.description);
            itemNode.put("source", item.source);
            itemNode.put("confidence", item.confidence);
            
            // 날짜 정보 추가
            if (item.effectiveDate != null && !item.effectiveDate.isEmpty()) {
                itemNode.put("effective_date", item.effectiveDate);
            }
            if (item.lastUpdated != null && !item.lastUpdated.isEmpty()) {
                itemNode.put("last_updated", item.lastUpdated);
            }
            
            ArrayNode keywords = objectMapper.createArrayNode();
            item.keywords.forEach(keywords::add);
            itemNode.set("keywords", keywords);
            
            array.add(itemNode);
        }
        
        return array;
    }

    /**
     * Citations 노드 구성
     */
    private ArrayNode buildCitationsNode(CollectedData data) {
        ArrayNode array = objectMapper.createArrayNode();
        
        for (Citation citation : data.citations) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("agency", citation.agency);
            node.put("category", citation.category);
            node.put("url", citation.url);
            node.put("title", citation.title);
            array.add(node);
        }
        
        return array;
    }

    /**
     * 원본 데이터 노드 구성
     */
    private ObjectNode buildRawDataNode(CollectedData data) {
        ObjectNode node = objectMapper.createObjectNode();
        
        if (data.fdaFoodEnforcement != null) {
            node.set("fda_food_enforcement", data.fdaFoodEnforcement);
        }
        if (data.usdaFdc != null) {
            node.set("usda_fdc", data.usdaFdc);
        }
        if (data.epaCompTox != null) {
            node.set("epa_comptox", data.epaCompTox);
        }
        if (data.censusImports != null) {
            node.set("census_imports", data.censusImports);
        }
        
        return node;
    }

    // ===== 내부 데이터 클래스 =====

    private static class CollectedData {
        JsonNode fdaFoodEnforcement;
        JsonNode usdaFdc;
        JsonNode epaCompTox;
        JsonNode censusImports;
        List<Citation> citations = new ArrayList<>();
    }

    private static class Citation {
        String agency;
        String category;
        String url;
        String title;

        Citation(String agency, String category, String url, String title) {
            this.agency = agency;
            this.category = category;
            this.url = url;
            this.title = title;
        }
    }

    private static class ExtractedRequirements {
        private List<RequirementItem> allItems = new ArrayList<>();

        void addItem(RequirementItem item) {
            allItems.add(item);
        }

        List<RequirementItem> getAllItems() {
            return allItems;
        }

        List<RequirementItem> getCertifications() {
            return allItems.stream()
                    .filter(item -> item.category.contains("certification") || 
                                   item.category.contains("compliance"))
                    .toList();
        }

        List<RequirementItem> getDocuments() {
            return allItems.stream()
                    .filter(item -> item.category.contains("document") || 
                                   item.category.contains("labeling"))
                    .toList();
        }

        List<RequirementItem> getNotices() {
            return allItems.stream()
                    .filter(item -> item.category.contains("notice") || 
                                   item.category.contains("enforcement"))
                    .toList();
        }

        int getTotalCount() {
            return allItems.size();
        }

        Map<String, Long> getCategoryStats() {
            return allItems.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            item -> item.category,
                            java.util.stream.Collectors.counting()
                    ));
        }
    }

    private static class RequirementItem {
        String agency;
        String category;
        String title;
        String description;
        String source;
        double confidence;
        List<String> keywords = List.of();
        String effectiveDate;  // 규정 발효일/보고일
        String lastUpdated;    // 마지막 업데이트일
    }
}

