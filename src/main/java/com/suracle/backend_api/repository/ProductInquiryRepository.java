package com.suracle.backend_api.repository;

import com.suracle.backend_api.entity.inquiry.ProductInquiry;
import com.suracle.backend_api.entity.inquiry.enums.InquiryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductInquiryRepository extends JpaRepository<ProductInquiry, Integer> {

    /**
     * 사용자별 문의 목록 조회
     */
    Page<ProductInquiry> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    /**
     * 상품별 문의 목록 조회
     */
    Page<ProductInquiry> findByProductIdOrderByCreatedAtDesc(Integer productId, Pageable pageable);

    /**
     * 문의 유형별 조회
     */
    Page<ProductInquiry> findByInquiryTypeOrderByCreatedAtDesc(InquiryType inquiryType, Pageable pageable);

    /**
     * 사용자별 문의 유형별 조회
     */
    Page<ProductInquiry> findByUserIdAndInquiryTypeOrderByCreatedAtDesc(
            Integer userId, InquiryType inquiryType, Pageable pageable);

    /**
     * 캐시에서 온 문의 조회
     */
    Page<ProductInquiry> findByFromCacheTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 사용자별 문의 개수 조회
     */
    long countByUserId(Integer userId);

    /**
     * 상품별 문의 개수 조회
     */
    long countByProductId(Integer productId);

    /**
     * 문의 유형별 문의 개수 조회
     */
    long countByInquiryType(InquiryType inquiryType);

    /**
     * 최근 문의 조회 (시간순)
     */
    @Query("SELECT pi FROM ProductInquiry pi ORDER BY pi.createdAt DESC")
    Page<ProductInquiry> findRecentInquiries(Pageable pageable);

    /**
     * AI 응답이 있는 문의 조회
     */
    @Query("SELECT pi FROM ProductInquiry pi WHERE pi.aiResponse IS NOT NULL ORDER BY pi.createdAt DESC")
    Page<ProductInquiry> findInquiriesWithAiResponse(Pageable pageable);
}
