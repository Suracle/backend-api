package com.suracle.backend_api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.suracle.backend_api.dto.precedents.PrecedentsResponseDto;
import com.suracle.backend_api.dto.product.ProductListResponseDto;
import com.suracle.backend_api.dto.product.ProductRequestDto;
import com.suracle.backend_api.dto.product.ProductResponseDto;
import com.suracle.backend_api.entity.cache.ProductAnalysisCache;
import com.suracle.backend_api.entity.product.Product;
import com.suracle.backend_api.entity.product.enums.ProductStatus;
import com.suracle.backend_api.entity.user.User;
import com.suracle.backend_api.repository.ProductAnalysisCacheRepository;
import com.suracle.backend_api.repository.ProductRepository;
import com.suracle.backend_api.repository.UserRepository;
import com.suracle.backend_api.service.AiWorkflowService;
import com.suracle.backend_api.service.ProductAnalysisService;
import com.suracle.backend_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductAnalysisCacheRepository productAnalysisCacheRepository;
    private final ProductAnalysisService productAnalysisService;
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
        
        log.info("상품 등록 완료 - 상품 ID: {}, 상품명: {}", savedProduct.getProductId(), savedProduct.getProductName());

        // 백그라운드 분석 스케줄링 (HS코드가 있는 경우에만)
        // AI 분석 임시 비활성화 - PostgreSQL JSON 타입 오류로 인해
        if (savedProduct.getHsCode() != null && !savedProduct.getHsCode().trim().isEmpty()) {
            log.info("HS코드가 존재하여 AI 워크플로우 실행 및 결과 저장 스케줄링 - 상품 ID: {}", savedProduct.getProductId());
            try {
                Map<String, Object> aiAnalysisResult = aiWorkflowService.executePrecedentsAnalysis(savedProduct);
                savePrecedentsAnalysisResult(savedProduct, aiAnalysisResult);
            } catch (Exception e) {
                log.error("AI 워크플로우 실행 및 결과 저장 실패 - 상품 ID: {}, 오류: {}", savedProduct.getProductId(), e.getMessage(), e);
            }
            // 기존 분석 서비스 비활성화
            // productAnalysisService.scheduleBackgroundAnalysis(savedProduct);
        }

        return convertToProductResponseDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDto> getProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::convertToProductListResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(String productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + productId));
        return convertToProductResponseDto(product);
    }

    @Transactional
    public ProductResponseDto updateProduct(String productId, ProductRequestDto requestDto, Integer sellerId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + productId));

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
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + productId));

        // 권한 확인
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("상품 삭제 권한이 없습니다.");
        }

        productRepository.delete(product);
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
        Page<Product> products;
        
        if (productName != null && !productName.trim().isEmpty() && status != null && !status.trim().isEmpty()) {
            products = productRepository.findBySellerIdAndProductNameContainingAndStatusAndIsActiveTrue(sellerId, productName, ProductStatus.valueOf(status), pageable);
        } else if (productName != null && !productName.trim().isEmpty()) {
            // 상품명만으로 검색하는 경우 - 기본 findAll 사용
            products = productRepository.findAll(pageable);
        } else if (status != null && !status.trim().isEmpty()) {
            // 상태만으로 검색하는 경우 - 기본 findAll 사용
            products = productRepository.findAll(pageable);
        } else {
            // 기본 검색 - 기본 findAll 사용
            products = productRepository.findAll(pageable);
        }
        
        return products.map(this::convertToProductListResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PrecedentsResponseDto getProductPrecedents(String productId) {
        log.info("상품 판례 조회 시작 - 상품 ID: {}", productId);
        
        try {
            // 상품 정보 조회
            Product product = productRepository.findByProductId(productId)
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + productId));
            
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

    private String generateProductId() {
        return "PROD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private ProductResponseDto convertToProductResponseDto(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .fobPrice(product.getFobPrice())
                .originCountry(product.getOriginCountry())
                .hsCode(product.getHsCode())
                .status(product.getStatus())
                .isActive(product.getIsActive())
                .sellerId(product.getSeller().getId())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductListResponseDto convertToProductListResponseDto(Product product) {
        return ProductListResponseDto.builder()
                .id(product.getId())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .fobPrice(product.getFobPrice())
                .originCountry(product.getOriginCountry())
                .hsCode(product.getHsCode())
                .status(product.getStatus())
                .isActive(product.getIsActive())
                .sellerId(product.getSeller().getId())
                .createdAt(product.getCreatedAt())
                .build();
    }

    private void savePrecedentsAnalysisResult(Product product, Map<String, Object> analysisResult) {
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
}
