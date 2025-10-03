package com.suracle.backend_api.controller;

import com.suracle.backend_api.service.AiWorkflowService;
import com.suracle.backend_api.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suracle.backend_api.entity.cache.ProductAnalysisCache;
import com.suracle.backend_api.entity.product.Product;
import com.suracle.backend_api.repository.ProductRepository;
import com.suracle.backend_api.repository.ProductAnalysisCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductAnalysisController {

    private final AiWorkflowService aiWorkflowService;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final ProductAnalysisCacheRepository productAnalysisCacheRepository;
    private final ObjectMapper objectMapper;

    /**
     * 상품 분석 실행 (요구사항, 관세, 판례)
     * @param productId 상품 ID
     * @return 분석 실행 결과
     */
    @PostMapping("/{productId}/analyze")
    public ResponseEntity<Map<String, Object>> triggerAnalysis(@PathVariable String productId) {
        try {
            log.info("상품 분석 실행 요청 - 상품 ID: {}", productId);
            
            // 상품 존재 확인
            Optional<Product> productOpt = productRepository.findByProductId(productId);
            if (productOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "상품을 찾을 수 없습니다: " + productId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Product product = productOpt.get();
            
            // HS코드 확인
            if (product.getHsCode() == null || product.getHsCode().trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "HS코드가 없어 분석을 실행할 수 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // AI 분석 실행
            Map<String, Object> analysisResult = aiWorkflowService.executePrecedentsAnalysis(product);
            
            // 결과 저장
            productService.saveAnalysisResult(product, analysisResult);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "상품 분석이 완료되었습니다.");
            response.put("productId", productId);
            response.put("analysisType", "precedents");
            response.put("confidenceScore", analysisResult.get("confidence_score"));
            response.put("isValid", analysisResult.get("is_valid"));
            
            log.info("상품 분석 완료 - 상품 ID: {}, 신뢰도: {}", productId, analysisResult.get("confidence_score"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("상품 분석 실행 실패 - 상품 ID: {}", productId, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "상품 분석 실행 중 오류가 발생했습니다: " + e.getMessage());
            response.put("productId", productId);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 요구사항 분석만 실행 (캐시 포함)
     * @param productId 상품 ID
     * @return 분석 실행 결과
     */
    @PostMapping("/{productId}/analyze/requirements")
    public ResponseEntity<Map<String, Object>> triggerRequirementsAnalysis(@PathVariable String productId) {
        try {
            log.info("요구사항 분석 실행 요청 - 상품 ID: {}", productId);
            
            // 상품 존재 확인
            Optional<Product> productOpt = productRepository.findByProductId(productId);
            if (productOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "상품을 찾을 수 없습니다: " + productId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Product product = productOpt.get();
            
            // HS코드 확인
            if (product.getHsCode() == null || product.getHsCode().trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "HS코드가 없어 분석을 실행할 수 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // AI 요구사항 분석 실행
            Map<String, Object> analysisResult = aiWorkflowService.executeRequirementsAnalysis(product);
            
            // 결과 저장
            productService.saveAnalysisResult(product, analysisResult);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "요구사항 분석이 완료되었습니다.");
            response.put("productId", productId);
            response.put("analysisType", "requirements");
            response.put("hsCode", product.getHsCode());
            response.put("productName", product.getProductName());
            
            // 요구사항 분석 결과 추가
            if (analysisResult.containsKey("recommended_agencies")) {
                response.put("recommendedAgencies", analysisResult.get("recommended_agencies"));
            }
            if (analysisResult.containsKey("llm_summary")) {
                response.put("llmSummary", analysisResult.get("llm_summary"));
            }
            if (analysisResult.containsKey("processing_time_ms")) {
                response.put("processingTimeMs", analysisResult.get("processing_time_ms"));
            }
            if (analysisResult.containsKey("cache_hit")) {
                response.put("cacheHit", analysisResult.get("cache_hit"));
            }
            
            log.info("요구사항 분석 완료 - 상품 ID: {}, HS코드: {}", productId, product.getHsCode());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("요구사항 분석 실행 실패 - 상품 ID: {}", productId, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "요구사항 분석 실행 중 오류가 발생했습니다: " + e.getMessage());
            response.put("productId", productId);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 관세 분석만 실행
     * @param productId 상품 ID
     * @return 분석 실행 결과
     */
    @PostMapping("/{productId}/analyze/tariff")
    public ResponseEntity<Map<String, Object>> triggerTariffAnalysis(@PathVariable String productId) {
        try {
            log.info("관세 분석 실행 요청 - 상품 ID: {}", productId);
            
            // 상품 존재 확인
            Optional<Product> productOpt = productRepository.findByProductId(productId);
            if (productOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "상품을 찾을 수 없습니다: " + productId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Product product = productOpt.get();
            
            // HS코드 확인
            if (product.getHsCode() == null || product.getHsCode().trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "HS코드가 없어 분석을 실행할 수 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // TODO: 관세 분석 서비스 호출
            // Map<String, Object> analysisResult = tariffAnalysisService.analyze(product);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "관세 분석이 완료되었습니다.");
            response.put("productId", productId);
            response.put("analysisType", "tariff");
            
            log.info("관세 분석 완료 - 상품 ID: {}", productId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("관세 분석 실행 실패 - 상품 ID: {}", productId, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "관세 분석 실행 중 오류가 발생했습니다: " + e.getMessage());
            response.put("productId", productId);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 세부 규정 분석 실행 (농약 잔류량, 화학성분 제한, 식품첨가물 기준, EMC 기준 등)
     * @param productId 상품 ID
     * @return 세부 규정 분석 결과
     */
    @PostMapping("/{productId}/analyze/detailed-regulations")
    public ResponseEntity<Map<String, Object>> triggerDetailedRegulationsAnalysis(@PathVariable String productId) {
        try {
            log.info("세부 규정 분석 실행 요청 - 상품 ID: {}", productId);
            
            // 상품 존재 확인
            Optional<Product> productOpt = productRepository.findByProductId(productId);
            if (productOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "상품을 찾을 수 없습니다: " + productId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Product product = productOpt.get();
            
            // HS코드 확인
            if (product.getHsCode() == null || product.getHsCode().trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "HS코드가 없어 분석을 실행할 수 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // AI 세부 규정 분석 실행
            Map<String, Object> analysisResult = aiWorkflowService.executeDetailedRegulationsAnalysis(product);
            
            // 결과 저장
            productService.saveAnalysisResult(product, analysisResult);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "세부 규정 분석이 완료되었습니다.");
            response.put("productId", productId);
            response.put("analysisType", "detailed_regulations");
            response.put("hsCode", product.getHsCode());
            response.put("productName", product.getProductName());
            
            // 세부 규정 분석 결과 추가
            if (analysisResult.containsKey("extracted_regulations")) {
                response.put("extractedRegulations", analysisResult.get("extracted_regulations"));
            }
            if (analysisResult.containsKey("phase_results")) {
                response.put("phaseResults", analysisResult.get("phase_results"));
            }
            if (analysisResult.containsKey("sources")) {
                response.put("sources", analysisResult.get("sources"));
            }
            if (analysisResult.containsKey("confidence")) {
                response.put("confidenceScore", analysisResult.get("confidence"));
            }
            
            log.info("세부 규정 분석 완료 - 상품 ID: {}, HS코드: {}", productId, product.getHsCode());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("세부 규정 분석 실행 실패 - 상품 ID: {}", productId, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "세부 규정 분석 실행 중 오류가 발생했습니다: " + e.getMessage());
            response.put("productId", productId);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 검사 절차 및 방법 분석만 실행 (Phase 2)
     */
    @PostMapping("/{productId}/analyze/testing-procedures")
    public ResponseEntity<Map<String, Object>> triggerTestingProceduresAnalysis(@PathVariable String productId) {
        try {
            log.info("검사 절차 분석 실행 요청 - 상품 ID: {}", productId);

            Optional<Product> productOpt = productRepository.findByProductId(productId);
            if (productOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "상품을 찾을 수 없습니다: " + productId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Product product = productOpt.get();
            if (product.getHsCode() == null || product.getHsCode().trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "HS코드가 없어 분석을 실행할 수 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Map<String, Object> analysisResult = aiWorkflowService.executeTestingProceduresAnalysis(product);

            productService.saveAnalysisResult(product, analysisResult);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "검사 절차 분석이 완료되었습니다.");
            response.put("productId", productId);
            response.put("analysisType", "testing_procedures");
            response.put("hsCode", product.getHsCode());
            response.put("productName", product.getProductName());
            response.putAll(analysisResult);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("검사 절차 분석 실행 실패 - 상품 ID: {}", productId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "검사 절차 분석 중 오류가 발생했습니다: " + e.getMessage());
            response.put("productId", productId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 처벌 및 벌금 분석만 실행 (Phase 3)
     */
    @PostMapping("/{productId}/analyze/penalties")
    public ResponseEntity<Map<String, Object>> triggerPenaltiesAnalysis(@PathVariable String productId) {
        try {
            log.info("처벌/벌금 분석 실행 요청 - 상품 ID: {}", productId);

            Optional<Product> productOpt = productRepository.findByProductId(productId);
            if (productOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "상품을 찾을 수 없습니다: " + productId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Product product = productOpt.get();
            if (product.getHsCode() == null || product.getHsCode().trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "HS코드가 없어 분석을 실행할 수 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Map<String, Object> analysisResult = aiWorkflowService.executePenaltiesAnalysis(product);
            productService.saveAnalysisResult(product, analysisResult);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "처벌/벌금 분석이 완료되었습니다.");
            response.put("productId", productId);
            response.put("analysisType", "penalties");
            response.put("hsCode", product.getHsCode());
            response.put("productName", product.getProductName());
            response.putAll(analysisResult);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("처벌/벌금 분석 실행 실패 - 상품 ID: {}", productId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "처벌/벌금 분석 중 오류가 발생했습니다: " + e.getMessage());
            response.put("productId", productId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 유효기간/갱신 분석만 실행 (Phase 4)
     */
    @PostMapping("/{productId}/analyze/validity")
    public ResponseEntity<Map<String, Object>> triggerValidityAnalysis(@PathVariable String productId) {
        try {
            log.info("유효기간/갱신 분석 실행 요청 - 상품 ID: {}", productId);

            Optional<Product> productOpt = productRepository.findByProductId(productId);
            if (productOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "상품을 찾을 수 없습니다: " + productId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Product product = productOpt.get();
            if (product.getHsCode() == null || product.getHsCode().trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "HS코드가 없어 분석을 실행할 수 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Map<String, Object> analysisResult = aiWorkflowService.executeValidityAnalysis(product);
            productService.saveAnalysisResult(product, analysisResult);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "유효기간/갱신 분석이 완료되었습니다.");
            response.put("productId", productId);
            response.put("analysisType", "validity");
            response.put("hsCode", product.getHsCode());
            response.put("productName", product.getProductName());
            response.putAll(analysisResult);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("유효기간/갱신 분석 실행 실패 - 상품 ID: {}", productId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "유효기간/갱신 분석 중 오류가 발생했습니다: " + e.getMessage());
            response.put("productId", productId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 분석 상태 확인
     * @param productId 상품 ID
     * @return 분석 상태
     */
    @GetMapping("/{productId}/analysis/status")
    public ResponseEntity<Map<String, Object>> getAnalysisStatus(@PathVariable String productId) {
        try {
            log.info("분석 상태 조회 요청 - 상품 ID: {}", productId);
            
            // 상품 존재 확인
            Optional<Product> productOpt = productRepository.findByProductId(productId);
            if (productOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "상품을 찾을 수 없습니다: " + productId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Product product = productOpt.get();
            
            // 분석 가능 여부 확인
            boolean hasHsCode = product.getHsCode() != null && !product.getHsCode().trim().isEmpty();
            
            // 분석 진행 상황 확인
            boolean precedentsComplete = productAnalysisCacheRepository
                    .findByProductIdAndAnalysisType(product.getId(), "precedents").isPresent();
            boolean requirementsComplete = productAnalysisCacheRepository
                    .findByProductIdAndAnalysisType(product.getId(), "requirements").isPresent();
            
            boolean analysisInProgress = hasHsCode && (!precedentsComplete || !requirementsComplete);
            boolean analysisComplete = hasHsCode && precedentsComplete && requirementsComplete;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("productId", productId);
            response.put("hasHsCode", hasHsCode);
            response.put("analysisAvailable", hasHsCode);
            response.put("analysisInProgress", analysisInProgress);
            response.put("analysisComplete", analysisComplete);
            response.put("precedentsComplete", precedentsComplete);
            response.put("requirementsComplete", requirementsComplete);
            response.put("productName", product.getProductName());
            response.put("hsCode", product.getHsCode());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("분석 상태 조회 실패 - 상품 ID: {}", productId, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "분석 상태 조회 중 오류가 발생했습니다: " + e.getMessage());
            response.put("productId", productId);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * HS코드로 분석 캐시 검색 (AI 엔진용)
     */
    @GetMapping("/analysis/search")
    public ResponseEntity<List<Map<String, Object>>> searchAnalysisByHsCode(
            @RequestParam String hs_code,
            @RequestParam String analysis_type) {
        try {
            log.info("HS코드로 분석 캐시 검색 - HS코드: {}, 분석타입: {}", hs_code, analysis_type);
            
            List<ProductAnalysisCache> caches = productAnalysisCacheRepository
                .findByProductHsCodeAndAnalysisType(hs_code, analysis_type);
            
            List<Map<String, Object>> results = caches.stream()
                .map(cache -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", cache.getId());
                    result.put("productId", cache.getProduct().getId());
                    result.put("analysisType", cache.getAnalysisType());
                    result.put("analysisResult", cache.getAnalysisResult());
                    result.put("confidenceScore", cache.getConfidenceScore());
                    result.put("isValid", cache.getIsValid());
                    result.put("createdAt", cache.getCreatedAt().toString());
                    result.put("updatedAt", cache.getUpdatedAt().toString());
                    return result;
                })
                .collect(Collectors.toList());
            
            log.info("분석 캐시 검색 완료 - 결과 수: {}", results.size());
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("HS코드로 분석 캐시 검색 실패 - HS코드: {}, 분석타입: {}", hs_code, analysis_type, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    /**
     * 분석 캐시 저장 (AI 엔진용)
     */
    @PostMapping("/analysis/cache")
    public ResponseEntity<Map<String, Object>> saveAnalysisCache(
            @RequestBody Map<String, Object> request) {
        try {
            log.info("분석 캐시 저장 요청: {}", request);
            
            String hsCode = (String) request.get("hsCode");
            String productName = (String) request.get("productName");
            String analysisType = (String) request.get("analysisType");
            Map<String, Object> analysisResult = (Map<String, Object>) request.get("analysisResult");
            Double confidenceScore = (Double) request.getOrDefault("confidenceScore", 0.95);
            Boolean isValid = (Boolean) request.getOrDefault("isValid", true);
            
            // HS코드로 상품 찾기
            Optional<Product> productOpt = productRepository.findByHsCode(hsCode);
            if (productOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "HS코드에 해당하는 상품을 찾을 수 없습니다: " + hsCode);
                return ResponseEntity.badRequest().body(response);
            }
            
            Product product = productOpt.get();
            
            // 기존 캐시 확인
            Optional<ProductAnalysisCache> existingCache = productAnalysisCacheRepository
                .findByProductIdAndAnalysisType(product.getId(), analysisType);
            
            ProductAnalysisCache cache;
            if (existingCache.isPresent()) {
                cache = existingCache.get();
                cache.setAnalysisResult(objectMapper.valueToTree(analysisResult));
                cache.setConfidenceScore(BigDecimal.valueOf(confidenceScore));
                cache.setIsValid(isValid);
                cache.setUpdatedAt(LocalDateTime.now());
            } else {
                cache = ProductAnalysisCache.builder()
                    .product(product)
                    .analysisType(analysisType)
                    .analysisResult(objectMapper.valueToTree(analysisResult))
                    .confidenceScore(BigDecimal.valueOf(confidenceScore))
                    .isValid(isValid)
                    .build();
            }
            
            productAnalysisCacheRepository.save(cache);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "분석 캐시 저장 완료");
            response.put("cacheId", cache.getId());
            
            log.info("분석 캐시 저장 완료 - 상품 ID: {}, 분석타입: {}", product.getId(), analysisType);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("분석 캐시 저장 실패", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "분석 캐시 저장 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 분석 캐시 삭제 (AI 엔진용)
     */
    @DeleteMapping("/analysis/cache")
    public ResponseEntity<Map<String, Object>> deleteAnalysisCache(
            @RequestParam String hs_code,
            @RequestParam String analysis_type) {
        try {
            log.info("분석 캐시 삭제 요청 - HS코드: {}, 분석타입: {}", hs_code, analysis_type);
            
            // HS코드로 상품 찾기
            Optional<Product> productOpt = productRepository.findByHsCode(hs_code);
            if (productOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "HS코드에 해당하는 상품을 찾을 수 없습니다: " + hs_code);
                return ResponseEntity.badRequest().body(response);
            }
            
            Product product = productOpt.get();
            
            // 캐시 삭제
            Optional<ProductAnalysisCache> cacheOpt = productAnalysisCacheRepository
                .findByProductIdAndAnalysisType(product.getId(), analysis_type);
            
            if (cacheOpt.isPresent()) {
                productAnalysisCacheRepository.delete(cacheOpt.get());
                log.info("분석 캐시 삭제 완료 - 상품 ID: {}, 분석타입: {}", product.getId(), analysis_type);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "분석 캐시 삭제 완료");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("분석 캐시 삭제 실패 - HS코드: {}, 분석타입: {}", hs_code, analysis_type, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "분석 캐시 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
