package com.suracle.backend_api.service;

import com.suracle.backend_api.dto.product.ProductListResponseDto;
import com.suracle.backend_api.dto.product.ProductRequestDto;
import com.suracle.backend_api.dto.precedents.PrecedentsResponseDto;
import com.suracle.backend_api.dto.product.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface ProductService {

    /**
     * 상품 등록
     * @param productRequestDto 상품 등록 요청 정보
     * @param sellerId 판매자 ID
     * @return 등록된 상품 정보
     */
    ProductResponseDto createProduct(ProductRequestDto productRequestDto, Integer sellerId);

    /**
     * 상품 목록 조회 (페이징)
     * @param pageable 페이징 정보
     * @return 상품 목록
     */
    Page<ProductListResponseDto> getProducts(Pageable pageable);

    /**
     * 상품 상세 조회
     * @param productId 상품 ID
     * @return 상품 상세 정보
     */
    ProductResponseDto getProductById(String productId);

    /**
     * 상품 상세 조회 (숫자 ID)
     * @param id 숫자 ID
     * @return 상품 상세 정보
     */
    Optional<ProductResponseDto> getProductById(Integer id);

    /**
     * 상품 삭제
     * @param productId 상품 ID
     * @param sellerId 판매자 ID (권한 확인용)
     */
    void deleteProduct(String productId, Integer sellerId);


    /**
     * 상품명으로 검색
     * @param productName 상품명
     * @param pageable 페이징 정보
     * @return 검색된 상품 목록
     */
    Page<ProductListResponseDto> searchProductsByName(String productName, Pageable pageable);


    /**
     * 판매자별 상품 검색 (상품명 + 상태 필터)
     * @param sellerId 판매자 ID
     * @param productName 상품명 (null이면 전체 상품명)
     * @param status 상품 상태 (null이면 전체)
     * @param pageable 페이징 정보
     * @return 검색된 상품 목록
     */
    Page<ProductListResponseDto> searchProductsBySellerIdAndNameAndStatus(Integer sellerId, String productName, String status, Pageable pageable);

    /**
     * 상품의 판례 분석 결과 조회
     * @param productId 상품 ID
     * @return 판례 분석 결과
     */
    PrecedentsResponseDto getProductPrecedents(String productId);
}
