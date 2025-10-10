package com.suracle.backend_api.repository;

import com.suracle.backend_api.entity.product.Product;
import com.suracle.backend_api.entity.product.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    /**
     * 상품 ID로 상품 조회
     */
    Optional<Product> findByProductId(String productId);


    /**
     * 활성화된 상품 목록 조회
     */
    Page<Product> findByIsActiveTrue(Pageable pageable);

    /**
     * 상품 상태별 조회
     */
    Page<Product> findByStatusAndIsActiveTrue(ProductStatus status, Pageable pageable);

    /**
     * 상품명으로 검색 (대소문자 구분 없음)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%')) AND p.isActive = true")
    Page<Product> findByProductNameContaining(@Param("productName") String productName, Pageable pageable);

    /**
     * 원산지로 검색
     */
    Page<Product> findByOriginCountryAndIsActiveTrue(String originCountry, Pageable pageable);

    /**
     * HS코드로 검색
     */
    Page<Product> findByHsCodeAndIsActiveTrue(String hsCode, Pageable pageable);

    /**
     * 판매자별 상품 개수 조회
     */
    long countBySellerIdAndIsActiveTrue(Integer sellerId);

    /**
     * 상품 ID 중복 확인
     */
    boolean existsByProductId(String productId);


    /**
     * 판매자별 상품 검색 (상품명 + 상태 필터, 대소문자 구분 없음)
     */
    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND (:productName IS NULL OR :productName = '' OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%'))) AND p.isActive = true AND (:status IS NULL OR p.status = :status)")
    Page<Product> findBySellerIdAndProductNameContainingAndStatusAndIsActiveTrue(@Param("sellerId") Integer sellerId, @Param("productName") String productName, @Param("status") ProductStatus status, Pageable pageable);

    /**
     * 상품명으로 검색 (대소문자 구분 없음, 단일 결과)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%')) AND p.isActive = true")
    Optional<Product> findByProductNameContainingIgnoreCase(@Param("productName") String productName);

    /**
     * HS코드로 상품 조회 (AI 엔진용)
     */
    Optional<Product> findByHsCode(String hsCode);

    /**
     * 활성 상태이고 HS코드가 있는 상품들 조회 (서버 시작 시 분석용)
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.hsCode IS NOT NULL AND p.hsCode != ''")
    List<Product> findByActiveAndHsCodeNotNull();

    
    /**
     * 특정 연도의 가장 최근 상품 ID 조회 (PROD-YYYY-### 형식에서 가장 큰 번호)
     */
    @Query("SELECT p.productId FROM Product p WHERE p.productId LIKE CONCAT('PROD-', :year, '-%') ORDER BY p.productId DESC")
    Optional<String> findLatestProductIdByYear(@Param("year") int year);
}
