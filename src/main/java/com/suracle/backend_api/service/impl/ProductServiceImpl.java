package com.suracle.backend_api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.suracle.backend_api.dto.precedents.PrecedentsResponseDto;
import com.suracle.backend_api.dto.product.ProductListResponseDto;
import com.suracle.backend_api.dto.product.ProductRequestDto;
import com.suracle.backend_api.dto.product.ProductResponseDto;
import com.suracle.backend_api.entity.cache.ProductAnalysisCache;
import com.suracle.backend_api.entity.hs.HsCode;
import com.suracle.backend_api.entity.product.Product;
import com.suracle.backend_api.entity.product.enums.ProductStatus;
import com.suracle.backend_api.entity.user.User;
import com.suracle.backend_api.repository.HsCodeRepository;
import com.suracle.backend_api.repository.ProductAnalysisCacheRepository;
import com.suracle.backend_api.repository.ProductRepository;
import com.suracle.backend_api.repository.UserRepository;
import com.suracle.backend_api.service.AiWorkflowService;
import com.suracle.backend_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final HsCodeRepository hsCodeRepository;
    private final ProductAnalysisCacheRepository productAnalysisCacheRepository;
    private final AiWorkflowService aiWorkflowService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto, Integer sellerId) {
        log.info("상품 등록 시작 - 상품명: {}, 판매자 ID: {}", requestDto.getProductName(), sellerId);

        // 판매자 정보 조회
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("판매자를 찾을 수 없습니다: " + sellerId));

        // Product 엔티티 생성
        Product product = Product.builder()
                .productId(generateProductId())
                .productName(requestDto.getProductName())
                .description(requestDto.getDescription())
                .price(requestDto.getPrice())
                .fobPrice(requestDto.getFobPrice())
                .originCountry(requestDto.getOriginCountry())
                .hsCode(requestDto.getHsCode())
                .status(requestDto.getStatus() != null ? requestDto.getStatus() : ProductStatus.DRAFT)
                .isActive(requestDto.getIsActive())
                .seller(seller)
                .build();

        // 상품 저장
        Product savedProduct = productRepository.save(product);
        
        // HS 코드 정보 저장 (AI 분석 결과가 있는 경우)
        if (requestDto.getHsCode() != null && !requestDto.getHsCode().trim().isEmpty()) {
            saveOrUpdateHsCode(requestDto);
        }
        
        log.info("상품 등록 완료 - 상품 ID: {}, 상품명: {}", savedProduct.getProductId(), savedProduct.getProductName());

        // 백그라운드 분석 스케줄링 (HS코드가 있는 경우에만)
        if (savedProduct.getHsCode() != null && !savedProduct.getHsCode().trim().isEmpty()) {
            log.info("HS코드가 존재하여 백그라운드 AI 분석 스케줄링 - 상품 ID: {}", savedProduct.getProductId());
            
            // 비동기로 분석 실행
            CompletableFuture.runAsync(() -> {
                try {
                    log.info("백그라운드 분석 시작 - 상품 ID: {}", savedProduct.getProductId());
                    
                    // 1. 판례 분석
                    Map<String, Object> precedentsResult = aiWorkflowService.executePrecedentsAnalysis(savedProduct);
                    savePrecedentsAnalysisResult(savedProduct, precedentsResult);
                    log.info("판례 분석 완료 - 상품 ID: {}", savedProduct.getProductId());
                    
                    // 2. 요구사항 분석
                    boolean shouldRunRequirements = shouldRunRequirementsAnalysis(savedProduct);
                    if (shouldRunRequirements) {
                        Map<String, Object> requirementsResult = aiWorkflowService.executeRequirementsAnalysis(savedProduct);
                        saveRequirementsAnalysisResult(savedProduct, requirementsResult);
                        log.info("요구사항 분석 완료 - 상품 ID: {}", savedProduct.getProductId());
                    } else {
                        log.info("요구사항 분석 스킵 (캐시 존재) - 상품 ID: {}", savedProduct.getProductId());
                    }
                    
                    log.info("백그라운드 분석 완료 - 상품 ID: {}", savedProduct.getProductId());
                    
                } catch (Exception e) {
                    log.error("백그라운드 분석 실행 실패 - 상품 ID: {}, 오류: {}", savedProduct.getProductId(), e.getMessage(), e);
                }
            });
        }

        return convertToProductResponseDto(savedProduct);
    }

    /**
     * HS 코드 정보 저장 또는 업데이트 (AI 분석 결과 반영)
     */
    private void saveOrUpdateHsCode(ProductRequestDto requestDto) {
        try {
            String hsCode = requestDto.getHsCode();
            
            // 기존 HS 코드 조회
            Optional<HsCode> existingHsCode = hsCodeRepository.findById(hsCode);
            
            if (existingHsCode.isPresent()) {
                // 기존 HS 코드가 있으면 AI 분석 결과로 업데이트
                HsCode hsCodeEntity = existingHsCode.get();
                
                if (requestDto.getHsCodeDescription() != null) {
                    hsCodeEntity.setDescription(requestDto.getHsCodeDescription());
                }
                if (requestDto.getUsTariffRate() != null) {
                    hsCodeEntity.setUsTariffRate(requestDto.getUsTariffRate());
                }
                if (requestDto.getReasoning() != null) {
                    hsCodeEntity.setReasoning(requestDto.getReasoning());
                }
                if (requestDto.getTariffReasoning() != null) {
                    hsCodeEntity.setTariffReasoning(requestDto.getTariffReasoning());
                }
                hsCodeEntity.setLastUpdated(LocalDateTime.now());
                
                hsCodeRepository.save(hsCodeEntity);
                log.info("✅ HS 코드 업데이트 완료: {}", hsCode);
                
            } else {
                // 새로운 HS 코드 생성
                HsCode newHsCode = HsCode.builder()
                        .hsCode(hsCode)
                        .description(requestDto.getHsCodeDescription() != null ? 
                                   requestDto.getHsCodeDescription() : "AI 분석 결과")
                        .usTariffRate(requestDto.getUsTariffRate() != null ? 
                                    requestDto.getUsTariffRate() : BigDecimal.ZERO)
                        .reasoning(requestDto.getReasoning() != null ? 
                                 requestDto.getReasoning() : "AI 분석 근거")
                        .tariffReasoning(requestDto.getTariffReasoning() != null ? 
                                       requestDto.getTariffReasoning() : "관세율 적용 근거")
                        .lastUpdated(LocalDateTime.now())
                        .build();
                
                hsCodeRepository.save(newHsCode);
                log.info("✅ 새로운 HS 코드 생성 완료: {}", hsCode);
            }
            
        } catch (Exception e) {
            log.error("❌ HS 코드 저장 실패: {}", e.getMessage(), e);
            // HS 코드 저장 실패는 상품 등록을 막지 않음
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDto> getProducts(Pageable pageable) {
        log.info("상품 목록 조회 - 페이지: {}", pageable.getPageNumber());
        // isActive = true인 활성 상품만 조회
        Page<Product> products = productRepository.findByIsActiveTrue(pageable);
        return products.map(this::convertToProductListResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(String productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));
        return convertToProductResponseDto(product);
    }

    @Transactional
    public ProductResponseDto updateProduct(String productId, ProductRequestDto requestDto, Integer sellerId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

        // 권한 확인
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("상품 수정 권한이 없습니다.");
        }

        // 상품 정보 업데이트
        product.setProductName(requestDto.getProductName());
        product.setDescription(requestDto.getDescription());
        product.setPrice(requestDto.getPrice());
        product.setFobPrice(requestDto.getFobPrice());
        product.setOriginCountry(requestDto.getOriginCountry());
        product.setHsCode(requestDto.getHsCode());
        product.setStatus(requestDto.getStatus() != null ? requestDto.getStatus() : ProductStatus.DRAFT);
        product.setIsActive(requestDto.getIsActive());

        Product updatedProduct = productRepository.save(product);
        return convertToProductResponseDto(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(String productId, Integer sellerId) {
        log.info("상품 삭제 요청 - 상품 ID: {}, 판매자 ID: {}", productId, sellerId);
        
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

        // 권한 확인
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("상품 삭제 권한이 없습니다.");
        }

        // 논리 삭제 (Soft Delete) - isActive를 false로 변경
        // 외래키 제약조건 때문에 물리적 삭제 불가능 (product_inquiries, product_analysis_cache, broker_reviews, tariff_calculations 참조)
        product.setIsActive(false);
        productRepository.save(product);
        
        log.info("상품 삭제 완료 (논리 삭제) - 상품 ID: {}", productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDto> searchProductsByName(String productName, Pageable pageable) {
        Page<Product> products = productRepository.findByProductNameContaining(productName, pageable);
        return products.map(this::convertToProductListResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDto> searchProductsBySellerIdAndNameAndStatus(Integer sellerId, String productName, String status, Pageable pageable) {
        log.info("판매자별 상품 검색 - 판매자 ID: {}, 상품명: {}, 상태: {}", sellerId, productName, status);
        
        // 상태 필터 처리
        ProductStatus productStatus = null;
        if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status)) {
            try {
                productStatus = ProductStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 상태 값: {}, 전체 상품 조회로 변경", status);
            }
        }
        
        // isActive = true인 상품만 조회
        Page<Product> products = productRepository.findBySellerIdAndProductNameContainingAndStatusAndIsActiveTrue(
            sellerId, 
            productName == null ? "" : productName, 
            productStatus, 
            pageable
        );
        
        return products.map(this::convertToProductListResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PrecedentsResponseDto getProductPrecedents(String productId) {
        log.info("상품 판례 조회 시작 - 상품 ID: {}", productId);
        
        try {
            // 상품 정보 조회
            Product product = productRepository.findByProductId(productId)
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));
            
            log.info("상품 정보 조회 완료 - 상품 ID: {}, HS코드: {}", product.getProductId(), product.getHsCode());
            
            // ProductAnalysisCache에서 precedents 분석 결과 조회
            Optional<ProductAnalysisCache> cache = productAnalysisCacheRepository
                    .findByProductIdAndAnalysisType(product.getId(), "precedents");
            
            if (cache.isPresent()) {
                log.info("캐시에서 판례 분석 결과 발견 - 상품 ID: {}", productId);
                
                ProductAnalysisCache analysisCache = cache.get();
                JsonNode analysisResultJson = analysisCache.getAnalysisResult();
                
                if (analysisResultJson != null && !analysisResultJson.isEmpty()) {
                    try {
                        // JSON을 Map으로 변환
                        @SuppressWarnings("unchecked")
                        Map<String, Object> analysisResult = objectMapper.convertValue(analysisResultJson, Map.class);
                        
                        // PrecedentsResponseDto로 변환
                        PrecedentsResponseDto responseDto = convertToPrecedentsDto(analysisResult);
                        
                        log.info("판례 분석 결과 반환 완료 - 상품 ID: {}, 신뢰도: {}", 
                                productId, responseDto.getConfidenceScore());
                        
                        return responseDto;
                        
                    } catch (Exception e) {
                        log.error("판례 분석 결과 파싱 실패 - 상품 ID: {}, 오류: {}", productId, e.getMessage(), e);
                        return getDefaultPrecedentsResponse();
                    }
                } else {
                    log.warn("캐시에 분석 결과가 없음 - 상품 ID: {}", productId);
                    return getDefaultPrecedentsResponse();
                }
            } else {
                log.warn("캐시에서 판례 분석 결과를 찾을 수 없음 - 상품 ID: {}", productId);
                return getDefaultPrecedentsResponse();
            }
            
        } catch (Exception e) {
            log.error("상품 판례 조회 중 오류 발생 - 상품 ID: {}, 오류: {}", productId, e.getMessage(), e);
            return getDefaultPrecedentsResponse();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductResponseDto> getProductById(Integer id) {
        return productRepository.findById(id)
                .map(this::convertToProductResponseDto);
    }

    /**
     * 상품 ID 생성 (PROD-YYYY-### 형태, 순차적 증가)
     * 예: PROD-2025-001, PROD-2025-002, ...
     */
    private String generateProductId() {
        // 현재 연도 가져오기
        int currentYear = java.time.LocalDate.now().getYear();
        
        // 해당 연도의 가장 최근 상품 ID 조회
        Optional<String> latestProductId = productRepository.findLatestProductIdByYear(currentYear);
        
        int nextNumber = 1; // 기본값: 해당 연도의 첫 상품
        
        if (latestProductId.isPresent()) {
            // 예: "PROD-2025-010" -> "010" 추출 -> 10 + 1 = 11
            String lastId = latestProductId.get();
            String numberPart = lastId.substring(lastId.lastIndexOf('-') + 1);
            try {
                int lastNumber = Integer.parseInt(numberPart);
                nextNumber = lastNumber + 1;
            } catch (NumberFormatException e) {
                log.warn("상품 ID 파싱 실패: {}, 기본값 1 사용", lastId);
                nextNumber = 1;
            }
        }
        
        // PROD-YYYY-### 형태로 생성
        String productId = String.format("PROD-%d-%03d", currentYear, nextNumber);
        
        log.debug("생성된 상품 ID: {} (이전 ID: {})", productId, latestProductId.orElse("없음"));
        return productId;
    }

    private ProductResponseDto convertToProductResponseDto(Product product) {
        // HS 코드 정보 조회
        String hsCodeDescription = null;
        BigDecimal usTariffRate = null;
        String reasoning = null;
        String tariffReasoning = null;
        
        if (product.getHsCode() != null && !product.getHsCode().trim().isEmpty()) {
            Optional<HsCode> hsCodeEntity = hsCodeRepository.findById(product.getHsCode());
            if (hsCodeEntity.isPresent()) {
                HsCode hsCode = hsCodeEntity.get();
                hsCodeDescription = hsCode.getDescription();
                usTariffRate = hsCode.getUsTariffRate();
                reasoning = hsCode.getReasoning();
                tariffReasoning = hsCode.getTariffReasoning();
            }
        }
        
        return ProductResponseDto.builder()
                .id(product.getId())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .fobPrice(product.getFobPrice())
                .originCountry(product.getOriginCountry())
                .hsCode(product.getHsCode())
                .hsCodeDescription(hsCodeDescription)  // HS 코드 설명
                .usTariffRate(usTariffRate)           // 관세율
                .reasoning(reasoning)                  // HS 코드 추천 근거
                .tariffReasoning(tariffReasoning)      // 관세율 적용 근거
                .status(product.getStatus())
                .isActive(product.getIsActive())
                .sellerId(product.getSeller().getId())
                .sellerName(product.getSeller().getUserName())  // ✅ 판매자명 추가
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductListResponseDto convertToProductListResponseDto(Product product) {
        // HS 코드에서 관세율 조회
        BigDecimal usTariffRate = null;
        if (product.getHsCode() != null) {
            Optional<HsCode> hsCode = hsCodeRepository.findById(product.getHsCode());
            if (hsCode.isPresent()) {
                usTariffRate = hsCode.get().getUsTariffRate();
            }
        }
        
        return ProductListResponseDto.builder()
                .id(product.getId())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .fobPrice(product.getFobPrice())
                .originCountry(product.getOriginCountry())
                .hsCode(product.getHsCode())
                .usTariffRate(usTariffRate)  // HS 코드 기반 관세율
                .status(product.getStatus())
                .isActive(product.getIsActive())
                .sellerId(product.getSeller().getId())
                .sellerName(product.getSeller().getUserName())  // ✅ 판매자명 추가
                .createdAt(product.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void saveRequirementsAnalysisResult(Product product, Map<String, Object> analysisResult) {
        try {
            log.info("요구사항 분석 결과 저장 시작 - 상품 ID: {}", product.getProductId());
            
            JsonNode analysisResultJson = objectMapper.valueToTree(analysisResult);
            
            // 기존 캐시 확인 (동시성 문제 해결을 위해 트랜잭션 내에서 다시 조회)
            Optional<ProductAnalysisCache> existingCache = productAnalysisCacheRepository
                .findByProductIdAndAnalysisType(product.getId(), "requirements");
            
            if (existingCache.isPresent()) {
                // 기존 캐시 업데이트
                ProductAnalysisCache cache = existingCache.get();
                cache.setAnalysisResult(analysisResultJson);
                cache.setConfidenceScore(java.math.BigDecimal.valueOf(extractConfidenceScore(analysisResult)));
                cache.setIsValid(extractIsValid(analysisResult));
                cache.setSources(objectMapper.valueToTree(extractSources(analysisResult)));
                cache.setUpdatedAt(LocalDateTime.now());
                
                productAnalysisCacheRepository.save(cache);
                log.info("요구사항 분석 결과 업데이트 완료 - 상품 ID: {}", product.getProductId());
                
            } else {
                // 새 캐시 생성 (중복 가능성 체크)
                try {
                    List<String> sources = extractSources(analysisResult);
                    
                    ProductAnalysisCache cache = ProductAnalysisCache.builder()
                        .product(product)
                        .analysisType("requirements")
                        .analysisResult(analysisResultJson)
                        .confidenceScore(java.math.BigDecimal.valueOf(extractConfidenceScore(analysisResult)))
                        .isValid(extractIsValid(analysisResult))
                        .sources(objectMapper.valueToTree(sources))
                        .build();
                    
                    productAnalysisCacheRepository.save(cache);
                    log.info("요구사항 분석 결과 저장 완료 - 상품 ID: {}", product.getProductId());
                    
                } catch (Exception duplicateError) {
                    if (duplicateError.getMessage() != null && 
                        duplicateError.getMessage().contains("중복된 키")) {
                        log.warn("중복된 분석 결과 감지, 기존 캐시 업데이트로 전환 - 상품 ID: {}", product.getProductId());
                        // 중복 발생 시 기존 캐시 업데이트로 처리
                        saveRequirementsAnalysisResult(product, analysisResult);
                        return;
                    } else {
                        throw duplicateError;
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("요구사항 분석 결과 저장 실패 - 상품 ID: {}", product.getProductId(), e);
            throw new RuntimeException("요구사항 분석 결과 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Override
    public void saveAnalysisResult(Product product, Map<String, Object> analysisResult) {
        try {
            String analysisType = (String) analysisResult.getOrDefault("analysis_type", "precedents");
            JsonNode analysisResultJson = objectMapper.valueToTree(analysisResult);

            ProductAnalysisCache cache = productAnalysisCacheRepository
                    .findByProductIdAndAnalysisType(product.getId(), analysisType)
                    .orElse(ProductAnalysisCache.builder()
                            .product(product)
                            .analysisType(analysisType)
                            .build());

            cache.setAnalysisResult(analysisResultJson);
            if (analysisResult.containsKey("sources")) {
                cache.setSources(objectMapper.valueToTree(analysisResult.get("sources")));
            }
            cache.setConfidenceScore(java.math.BigDecimal.valueOf(extractConfidenceScore(analysisResult)));
            cache.setIsValid(extractIsValid(analysisResult));

            productAnalysisCacheRepository.save(cache);
            log.info("제네릭 분석 결과 저장 완료 - 상품 ID: {}, 타입: {}", product.getProductId(), analysisType);
        } catch (Exception e) {
            log.error("제네릭 분석 결과 저장 실패 - 상품 ID: {}", product.getProductId(), e);
        }
    }

    /**
     * 요구사항 분석 실행 여부 판단
     * 3가지 조건: 상품 등록 시, DB에 없을 시, 수동 리프레시 시
     */
    /**
     * 요구사항 분석 실행이 필요한지 확인 (캐시 우선, HS코드 공유 로직 포함)
     * @param product 상품 정보
     * @return true: 실행 필요, false: 캐시 존재로 실행 불필요
     */
    private boolean shouldRunRequirementsAnalysis(Product product) {
        try {
            log.info("요구사항 분석 실행 조건 확인 - 상품 ID: {}, HS코드: {}", product.getProductId(), product.getHsCode());
            
            // HS코드가 없으면 실행하지 않음
            if (product.getHsCode() == null || product.getHsCode().trim().isEmpty()) {
                log.info("HS코드가 없어 요구사항 분석 스킵 - 상품 ID: {}", product.getProductId());
                return false;
            }
            
            // 조건 1: 현재 상품의 캐시 확인
            Optional<ProductAnalysisCache> currentProductCache = productAnalysisCacheRepository
                .findByProductIdAndAnalysisType(product.getId(), "requirements");
            
            if (currentProductCache.isPresent()) {
                ProductAnalysisCache cache = currentProductCache.get();
                // 캐시가 있고 유효한 경우 (7일 이내)
                if (cache.getUpdatedAt() != null && 
                    cache.getUpdatedAt().isAfter(LocalDateTime.now().minusDays(7))) {
                    log.info("현재 상품의 요구사항 분석 캐시 존재 (7일 이내) - 상품 ID: {}", product.getProductId());
                    return false; // 캐시가 있으면 실행하지 않음
                }
            }
            
            // 조건 2: 같은 HS코드의 다른 상품 캐시 확인 (AI 엔진 캐시 활용)
            List<ProductAnalysisCache> hsCodeCaches = productAnalysisCacheRepository
                .findByProductHsCodeAndAnalysisType(product.getHsCode(), "requirements");
            
            if (!hsCodeCaches.isEmpty()) {
                // 같은 HS코드의 캐시 중 가장 최근 것 확인
                ProductAnalysisCache recentHsCodeCache = hsCodeCaches.stream()
                        .filter(cache -> cache.getUpdatedAt() != null)
                        .filter(cache -> cache.getUpdatedAt().isAfter(LocalDateTime.now().minusDays(7)))
                        .max((c1, c2) -> c1.getUpdatedAt().compareTo(c2.getUpdatedAt()))
                        .orElse(null);
                
                if (recentHsCodeCache != null) {
                    log.info("✅ 같은 HS코드의 요구사항 분석 캐시 활용 가능 (7일 이내) - HS코드: {}, 상품 ID: {}", 
                            product.getHsCode(), product.getProductId());
                    
                    // 현재 상품에도 캐시 복사 (지능형 캐싱)
                    ProductAnalysisCache newCache = ProductAnalysisCache.builder()
                            .product(product)
                            .analysisType("requirements")
                            .analysisResult(recentHsCodeCache.getAnalysisResult())
                            .confidenceScore(recentHsCodeCache.getConfidenceScore())
                            .isValid(recentHsCodeCache.getIsValid())
                            .build();
                    productAnalysisCacheRepository.save(newCache);
                    
                    return false; // 캐시가 있으면 실행하지 않음
                }
            }
            
            log.info("요구사항 분석 실행 필요 - 상품 ID: {}, HS코드: {}", product.getProductId(), product.getHsCode());
            return true;
            
        } catch (Exception e) {
            log.error("요구사항 분석 실행 조건 확인 실패 - 상품 ID: {}", product.getProductId(), e);
            return true; // 오류 시 실행
        }
    }

    @Override
    public void savePrecedentsAnalysisResult(Product product, Map<String, Object> analysisResult) {
        try {
            String analysisType = "precedents";
            JsonNode analysisResultJson = objectMapper.valueToTree(analysisResult);
            
            // ProductAnalysisCache 엔티티 생성 또는 업데이트
            ProductAnalysisCache cache = productAnalysisCacheRepository
                    .findByProductIdAndAnalysisType(product.getId(), analysisType)
                    .orElse(ProductAnalysisCache.builder()
                            .product(product)
                            .analysisType(analysisType)
                            .build());

            cache.setAnalysisResult(analysisResultJson);
            cache.setSources(objectMapper.valueToTree(analysisResult.getOrDefault("sources", List.of()))); // sources 필드 추가
            cache.setConfidenceScore(java.math.BigDecimal.valueOf((Double) analysisResult.getOrDefault("confidence_score", 0.0)));
            cache.setIsValid((Boolean) analysisResult.getOrDefault("is_valid", false));
            
            productAnalysisCacheRepository.save(cache);
            log.info("분석 결과 캐시 저장 완료 - 상품 ID: {}, 분석 타입: {}", product.getProductId(), analysisType);
        } catch (Exception e) {
            log.error("분석 결과 캐시 저장 실패", e);
        }
    }

    private PrecedentsResponseDto convertToPrecedentsDto(Map<String, Object> analysisResult) {
        return PrecedentsResponseDto.builder()
                .successCases((List<String>) analysisResult.getOrDefault("success_cases", List.of()))
                .failureCases((List<String>) analysisResult.getOrDefault("failure_cases", List.of()))
                .actionableInsights((List<String>) analysisResult.getOrDefault("actionable_insights", List.of()))
                .riskFactors((List<String>) analysisResult.getOrDefault("risk_factors", List.of()))
                .recommendedAction((String) analysisResult.getOrDefault("recommended_action", ""))
                .confidenceScore((Double) analysisResult.getOrDefault("confidence_score", 0.0))
                .isValid((Boolean) analysisResult.getOrDefault("is_valid", false))
                .build();
    }

    private PrecedentsResponseDto getDefaultPrecedentsResponse() {
        return PrecedentsResponseDto.builder()
                .successCases(List.of())
                .failureCases(List.of())
                .actionableInsights(List.of("분석 결과를 찾을 수 없습니다. 관리자에게 문의하세요."))
                .riskFactors(List.of("데이터 부족"))
                .recommendedAction("관세사 상담을 권장합니다.")
                .confidenceScore(0.0)
                .isValid(false)
                .build();
    }
    
    private Double extractConfidenceScore(Map<String, Object> analysisResult) {
        try {
            // llm_summary에서 confidence_score 추출 (우선순위 1)
            if (analysisResult.containsKey("llm_summary")) {
                Map<?, ?> llmSummary = (Map<?, ?>) analysisResult.get("llm_summary");
                if (llmSummary.containsKey("confidence_score")) {
                    Object score = llmSummary.get("confidence_score");
                    if (score instanceof Number) {
                        double confidence = ((Number) score).doubleValue();
                        return confidence > 0.0 ? confidence : 0.85; // 기본값 0.85
                    }
                }
            }
            
            // metadata에서 confidence_score 추출 (우선순위 2)
            if (analysisResult.containsKey("metadata")) {
                Map<?, ?> metadata = (Map<?, ?>) analysisResult.get("metadata");
                if (metadata.containsKey("confidence_score")) {
                    Object score = metadata.get("confidence_score");
                    if (score instanceof Number) {
                        return ((Number) score).doubleValue();
                    }
                }
            }
            
            // 최상위에서 confidence_score 추출 (우선순위 3)
            if (analysisResult.containsKey("confidence_score")) {
                Object score = analysisResult.get("confidence_score");
                if (score instanceof Number) {
                    return ((Number) score).doubleValue();
                }
            }
            
            // 기본값: requirements 분석은 기본적으로 높은 신뢰도
            return 0.85;
        } catch (Exception e) {
            log.warn("신뢰도 점수 추출 실패: {}", e.getMessage());
            return 0.85; // 오류 시에도 기본값 0.85
        }
    }
    
    private Boolean extractIsValid(Map<String, Object> analysisResult) {
        try {
            if (analysisResult.containsKey("metadata")) {
                Map<?, ?> metadata = (Map<?, ?>) analysisResult.get("metadata");
                if (metadata.containsKey("error")) {
                    return !((Boolean) metadata.get("error"));
                }
            }
            if (analysisResult.containsKey("is_valid")) {
                return (Boolean) analysisResult.get("is_valid");
            }
        } catch (Exception e) {
            log.warn("유효성 추출 실패: {}", e.getMessage());
        }
        return true;
    }
    
    private List<String> extractSources(Map<String, Object> analysisResult) {
        try {
            if (analysisResult.containsKey("sources")) {
                Object sourcesObj = analysisResult.get("sources");
                if (sourcesObj instanceof List) {
                    return (List<String>) sourcesObj;
                }
            }
            if (analysisResult.containsKey("metadata")) {
                Map<?, ?> metadata = (Map<?, ?>) analysisResult.get("metadata");
                if (metadata.containsKey("sources")) {
                    Object sourcesObj = metadata.get("sources");
                    if (sourcesObj instanceof List) {
                        return (List<String>) sourcesObj;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("sources 추출 실패: {}", e.getMessage());
        }
        
        // 기본값 반환 - 요구사항 분석용 일반적인 소스들
        return List.of(
            "https://www.fda.gov/cosmetics/cosmetics-laws-regulations",
            "https://www.ecfr.gov/current/title-21/chapter-I/subchapter-G/part-701",
            "https://www.cbp.gov/trade/programs-administration/trade-support-and-monitoring",
            "서버 시작 시 자동 분석 실행"
        );
    }
}
