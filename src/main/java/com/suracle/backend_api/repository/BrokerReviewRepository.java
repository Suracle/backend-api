package com.suracle.backend_api.repository;

import com.suracle.backend_api.entity.broker.BrokerReview;
import com.suracle.backend_api.entity.broker.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrokerReviewRepository extends JpaRepository<BrokerReview, Integer> {

    /**
     * 관세사별 리뷰 목록 조회
     */
    Page<BrokerReview> findByBrokerIdOrderByCreatedAtDesc(Integer brokerId, Pageable pageable);

    /**
     * 상품별 리뷰 목록 조회
     */
    Page<BrokerReview> findByProductIdOrderByCreatedAtDesc(Integer productId, Pageable pageable);

    /**
     * 리뷰 상태별 조회
     */
    Page<BrokerReview> findByReviewStatusOrderByCreatedAtDesc(ReviewStatus reviewStatus, Pageable pageable);

    /**
     * 관세사별 리뷰 상태별 조회
     */
    Page<BrokerReview> findByBrokerIdAndReviewStatusOrderByCreatedAtDesc(
            Integer brokerId, ReviewStatus reviewStatus, Pageable pageable);

    /**
     * 상품과 관세사로 리뷰 조회
     */
    Optional<BrokerReview> findByProductIdAndBrokerId(Integer productId, Integer brokerId);

    /**
     * 상품별 리뷰 개수 조회
     */
    long countByProductId(Integer productId);

    /**
     * 관세사별 리뷰 개수 조회
     */
    long countByBrokerId(Integer brokerId);

    /**
     * 리뷰 상태별 리뷰 개수 조회
     */
    long countByReviewStatus(ReviewStatus reviewStatus);

    /**
     * 대기 중인 리뷰 목록 조회 (관세사별)
     */
    @Query("SELECT br FROM BrokerReview br WHERE br.broker.id = :brokerId ORDER BY br.requestedAt ASC")
    List<BrokerReview> findPendingReviewsByBrokerId(@Param("brokerId") Integer brokerId);

    /**
     * 상품별 최신 리뷰 조회
     */
    @Query("SELECT br FROM BrokerReview br WHERE br.product.id = :productId ORDER BY br.createdAt DESC")
    Page<BrokerReview> findLatestReviewsByProductId(@Param("productId") Integer productId, Pageable pageable);
}
