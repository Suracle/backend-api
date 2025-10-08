package com.suracle.backend_api.service;

import com.suracle.backend_api.dto.broker.BrokerReviewListResponseDto;
import com.suracle.backend_api.dto.broker.BrokerReviewRequestDto;
import com.suracle.backend_api.dto.broker.BrokerReviewResponseDto;
import com.suracle.backend_api.entity.broker.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BrokerReviewService {

    /**
     * 리뷰 요청 생성
     * @param requestDto 리뷰 요청 정보
     * @return 생성된 리뷰 정보
     */
    BrokerReviewResponseDto createReviewRequest(BrokerReviewRequestDto requestDto);

    /**
     * 리뷰 상태 업데이트 (관세사가 리뷰 처리)
     * @param reviewId 리뷰 ID
     * @param reviewStatus 새로운 리뷰 상태
     * @param reviewComment 리뷰 코멘트
     * @param suggestedHsCode 제안된 HS코드
     * @return 업데이트된 리뷰 정보
     */
    BrokerReviewResponseDto updateReviewStatus(Integer reviewId, ReviewStatus reviewStatus, 
                                               String reviewComment, String suggestedHsCode);

    /**
     * 리뷰 상세 조회
     * @param reviewId 리뷰 ID
     * @return 리뷰 상세 정보
     */
    BrokerReviewResponseDto getReviewById(Integer reviewId);

    /**
     * 관세사별 리뷰 목록 조회
     * @param brokerId 관세사 ID
     * @param pageable 페이징 정보
     * @return 관세사 리뷰 목록
     */
    Page<BrokerReviewListResponseDto> getReviewsByBrokerId(Integer brokerId, Pageable pageable);

    /**
     * 상품별 리뷰 목록 조회
     * @param productId 상품 ID
     * @param pageable 페이징 정보
     * @return 상품 리뷰 목록
     */
    Page<BrokerReviewListResponseDto> getReviewsByProductId(Integer productId, Pageable pageable);

    /**
     * 리뷰 상태별 목록 조회
     * @param reviewStatus 리뷰 상태
     * @param pageable 페이징 정보
     * @return 리뷰 목록
     */
    Page<BrokerReviewListResponseDto> getReviewsByStatus(ReviewStatus reviewStatus, Pageable pageable);

    /**
     * 관세사별 대기 중인 리뷰 목록 조회
     * @param brokerId 관세사 ID
     * @return 대기 중인 리뷰 목록
     */
    List<BrokerReviewListResponseDto> getPendingReviewsByBrokerId(Integer brokerId);

    /**
     * 리뷰 삭제
     * @param reviewId 리뷰 ID
     * @param brokerId 관세사 ID (권한 확인용)
     */
    void deleteReview(Integer reviewId, Integer brokerId);
    
    /**
     * 상품의 최신 리뷰 조회 (단일)
     * @param productId 상품 ID
     * @return 최신 리뷰 정보 (없으면 null)
     */
    BrokerReviewResponseDto getLatestReviewByProductId(Integer productId);
}
