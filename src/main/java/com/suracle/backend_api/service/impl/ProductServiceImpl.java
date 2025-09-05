package com.suracle.backend_api.service.impl;

import com.suracle.backend_api.dto.product.ProductListResponseDto;
import com.suracle.backend_api.dto.product.ProductRequestDto;
import com.suracle.backend_api.dto.product.ProductResponseDto;
import com.suracle.backend_api.entity.product.Product;
import com.suracle.backend_api.entity.product.enums.ProductStatus;
import com.suracle.backend_api.entity.user.User;
import com.suracle.backend_api.repository.ProductRepository;
import com.suracle.backend_api.repository.UserRepository;
import com.suracle.backend_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public ProductResponseDto createProduct(ProductRequestDto productRequestDto, Integer sellerId) {
        log.info("상품 등록 요청 - 판매자 ID: {}, 상품명: {}", sellerId, productRequestDto.getProductName());

        // 판매자 존재 확인
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 판매자입니다: " + sellerId));

        // 상품 ID 생성 (UUID 기반)
        String productId = generateProductId();

        // Product 엔티티 생성
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

        // 상품 저장
        Product savedProduct = productRepository.save(product);
        log.info("상품 등록 완료 - 상품 ID: {}, 상품명: {}", savedProduct.getProductId(), savedProduct.getProductName());

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

    /**
     * 상품 ID 생성 (UUID 기반)
     */
    private String generateProductId() {
        String productId;
        do {
            productId = "PRD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (productRepository.existsByProductId(productId));
        return productId;
    }

    /**
     * Product 엔티티를 ProductResponseDto로 변환
     */
    private ProductResponseDto convertToProductResponseDto(Product product) {
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
