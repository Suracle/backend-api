package com.suracle.backend_api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suracle.backend_api.dto.product.ProductListResponseDto;
import com.suracle.backend_api.dto.product.ProductRequestDto;
import com.suracle.backend_api.dto.product.ProductResponseDto;
import com.suracle.backend_api.dto.precedents.PrecedentsResponseDto;
import com.suracle.backend_api.entity.cache.ProductAnalysisCache;
import com.suracle.backend_api.entity.product.Product;
import com.suracle.backend_api.entity.product.enums.ProductStatus;
import com.suracle.backend_api.entity.user.User;
import com.suracle.backend_api.repository.ProductAnalysisCacheRepository;
import com.suracle.backend_api.repository.ProductRepository;
import com.suracle.backend_api.repository.UserRepository;
import com.suracle.backend_api.repository.HsCodeRepository;
import com.suracle.backend_api.service.ProductService;
import com.suracle.backend_api.service.ProductAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final HsCodeRepository hsCodeRepository;
    private final ProductAnalysisService productAnalysisService;
    private final ProductAnalysisCacheRepository productAnalysisCacheRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ProductResponseDto createProduct(ProductRequestDto productRequestDto, Integer sellerId) {
        log.info("상품 등록 요청 - 판매자 ID: {}, 상품명: {}, 가격: {}, FOB가격: {}", 
                sellerId, productRequestDto.getProductName(), productRequestDto.getPrice(), productRequestDto.getFobPrice());

        // 판매자 존재 확인
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 판매자 ID: {}", sellerId);
                    return new IllegalArgumentException("존재하지 않는 판매자입니다: " + sellerId);
                });

        // 상품 ID 생성 (UUID 기반)
        String productId = generateProductId();

        // Product 엔티티 생성
        Product savedProduct;
        try {
            Product product = Product.builder()
                    .seller(seller)
                    .productId(productId)
                    .productName(productRequestDto.getProductName())
                    .description(productRequestDto.getDescription())
                    .price(productRequestDto.getPrice())
                    .fobPrice(productRequestDto.getFobPrice())
                    .originCountry(productRequestDto.getOriginCountry())
                    .hsCode(productRequestDto.getHsCode())
                    .status(productRequestDto.getStatus() != null ? productRequestDto.getStatus() : ProductStatus.DRAFT)
                    .isActive(productRequestDto.getIsActive() != null ? productRequestDto.getIsActive() : true)
                    .build();

            log.info("Product 엔티티 생성 완료 - ID: {}, 상품명: {}, 가격: {}, FOB가격: {}", 
                    productId, product.getProductName(), product.getPrice(), product.getFobPrice());

            // 상품 저장
            savedProduct = productRepository.save(product);
            log.info("상품 저장 완료 - DB ID: {}, 상품 ID: {}", savedProduct.getId(), savedProduct.getProductId());
        } catch (Exception e) {
            log.error("Product 엔티티 생성 또는 저장 중 오류 발생 - 판매자 ID: {}, 상품명: {}, 오류: {}", 
                     sellerId, productRequestDto.getProductName(), e.getMessage(), e);
            throw e;
        }
        
        log.info("상품 등록 완료 - 상품 ID: {}, 상품명: {}", savedProduct.getProductId(), savedProduct.getProductName());

        // 백그라운드 분석 스케줄링 (HS코드가 있는 경우에만)
        if (savedProduct.getHsCode() != null && !savedProduct.getHsCode().trim().isEmpty()) {
            productAnalysisService.scheduleBackgroundAnalysis(savedProduct);
        }

        return convertToProductResponseDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDto> getProducts(Pageable pageable) {
        log.info("상품 목록 조회 요청 - 페이지: {}, 크기: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> products = productRepository.findByIsActiveTrue(pageable);
        return products.map(this::convertToProductListResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(String productId) {
        log.info("상품 상세 조회 요청 - 상품 ID: {}", productId);

        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + productId));

        if (!product.getIsActive()) {
            throw new IllegalArgumentException("비활성화된 상품입니다: " + productId);
        }

        return convertToProductResponseDto(product);
    }

    @Override
    public void deleteProduct(String productId, Integer sellerId) {
        log.info("상품 삭제 요청 - 상품 ID: {}, 판매자 ID: {}", productId, sellerId);

        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + productId));

        // 권한 확인 (본인 상품인지 확인)
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new IllegalArgumentException("상품 삭제 권한이 없습니다");
        }

        // 논리 삭제 (isActive를 false로 변경)
        product.setIsActive(false);
        productRepository.save(product);

        log.info("상품 삭제 완료 - 상품 ID: {}", productId);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDto> searchProductsByName(String productName, Pageable pageable) {
        log.info("상품명 검색 요청 - 검색어: {}, 페이지: {}", productName, pageable.getPageNumber());

        Page<Product> products = productRepository.findByProductNameContaining(productName, pageable);
        return products.map(this::convertToProductListResponseDto);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDto> searchProductsBySellerIdAndNameAndStatus(Integer sellerId, String productName, String status, Pageable pageable) {
        log.info("판매자별 상품 검색 요청 (상품명 + 상태 필터) - 판매자 ID: {}, 검색어: {}, 상태: {}, 페이지: {}", sellerId, productName, status, pageable.getPageNumber());

        // 상태 필터 처리
        ProductStatus productStatus = null;
        if (status != null && !status.equals("all") && !status.isEmpty()) {
            try {
                productStatus = ProductStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 상품 상태: {}", status);
                throw new IllegalArgumentException("잘못된 상품 상태입니다: " + status);
            }
        }

        // 검색어 처리 (null이거나 빈 문자열이면 전체 상품명으로 처리)
        String searchTerm = (productName == null || productName.trim().isEmpty()) ? null : productName;

        Page<Product> products = productRepository.findBySellerIdAndProductNameContainingAndStatusAndIsActiveTrue(sellerId, searchTerm, productStatus, pageable);
        return products.map(this::convertToProductListResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PrecedentsResponseDto getProductPrecedents(String productId) {
        log.info("상품 판례 분석 조회 요청 - 상품 ID: {}", productId);

        // 상품 존재 확인
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + productId));

        // precedents 분석 결과 조회
        ProductAnalysisCache precedentsCache = productAnalysisCacheRepository
                .findByProductIdAndAnalysisType(product.getId(), "precedents")
                .orElseThrow(() -> new IllegalArgumentException("해당 상품의 판례 분석 결과가 없습니다"));

        try {
            // JSON 분석 결과를 Map으로 파싱
            Map<String, Object> analysisResult = objectMapper.readValue(
                    precedentsCache.getAnalysisResult(), 
                    Map.class
            );

            // PrecedentsResponseDto 생성 - data.sql 구조에 맞게 수정
            return PrecedentsResponseDto.builder()
                    .successCases((List<String>) analysisResult.get("success_cases"))
                    .failureCases((List<String>) analysisResult.get("failure_cases"))
                    .actionableInsights((List<String>) analysisResult.get("actionable_insights"))
                    .riskFactors((List<String>) analysisResult.get("risk_factors"))
                    .recommendedAction((String) analysisResult.get("recommended_action"))
                    .confidenceScore(precedentsCache.getConfidenceScore().doubleValue())
                    .isValid(precedentsCache.getIsValid())
                    .build();

        } catch (JsonProcessingException e) {
            log.error("판례 분석 결과 파싱 오류 - 상품 ID: {}", productId, e);
            throw new RuntimeException("판례 분석 결과를 처리하는 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 상품 ID 생성 (PROD-YYYY-#### 형태)
     */
    private String generateProductId() {
        String productId;
        do {
            // 현재 연도 가져오기
            int currentYear = java.time.LocalDate.now().getYear();
            
            // 4자리 랜덤 숫자 생성 (0001-9999)
            int randomNumber = (int) (Math.random() * 9999) + 1;
            
            // PROD-YYYY-#### 형태로 생성
            productId = String.format("PROD-%d-%04d", currentYear, randomNumber);
        } while (productRepository.existsByProductId(productId));
        return productId;
    }

    /**
     * Product 엔티티를 ProductResponseDto로 변환
     */
    private ProductResponseDto convertToProductResponseDto(Product product) {
        // HS 코드 설명 조회
        String hsCodeDescription = null;
        if (product.getHsCode() != null) {
            try {
                hsCodeDescription = hsCodeRepository.findByHsCode(product.getHsCode())
                        .map(hsCode -> hsCode.getDescription())
                        .orElse(null);
            } catch (Exception e) {
                log.warn("HS 코드 설명 조회 실패 - HS 코드: {}, 오류: {}", product.getHsCode(), e.getMessage());
            }
        }

        return ProductResponseDto.builder()
                .id(product.getId())
                .sellerId(product.getSeller().getId())
                .sellerName(product.getSeller().getUserName())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .fobPrice(product.getFobPrice())
                .originCountry(product.getOriginCountry())
                .hsCode(product.getHsCode())
                .hsCodeDescription(hsCodeDescription)
                .status(product.getStatus())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    /**
     * Product 엔티티를 ProductListResponseDto로 변환
     */
    private ProductListResponseDto convertToProductListResponseDto(Product product) {
        return ProductListResponseDto.builder()
                .id(product.getId())
                .sellerId(product.getSeller().getId())
                .sellerName(product.getSeller().getUserName())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .fobPrice(product.getFobPrice())
                .originCountry(product.getOriginCountry())
                .hsCode(product.getHsCode())
                .status(product.getStatus())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
