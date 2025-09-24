package com.suracle.backend_api.service.impl;

import com.suracle.backend_api.dto.broker.BrokerReviewListResponseDto;
import com.suracle.backend_api.dto.broker.BrokerReviewRequestDto;
import com.suracle.backend_api.dto.broker.BrokerReviewResponseDto;
import com.suracle.backend_api.entity.broker.BrokerReview;
import com.suracle.backend_api.entity.broker.enums.ReviewStatus;
import com.suracle.backend_api.entity.product.Product;
import com.suracle.backend_api.entity.user.User;
import com.suracle.backend_api.repository.BrokerReviewRepository;
import com.suracle.backend_api.repository.ProductRepository;
import com.suracle.backend_api.repository.UserRepository;
import com.suracle.backend_api.service.BrokerReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BrokerReviewServiceImpl implements BrokerReviewService {

    private final BrokerReviewRepository brokerReviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public BrokerReviewResponseDto createReviewRequest(BrokerReviewRequestDto requestDto) {
        log.info("리뷰 요청 생성 - 상품 ID: {}, 관세사 ID: {}", requestDto.getProductId(), requestDto.getBrokerId());

        // 상품 존재 확인
        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + requestDto.getProductId()));

        // 관세사 존재 확인
        User broker = userRepository.findById(requestDto.getBrokerId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관세사입니다: " + requestDto.getBrokerId()));

        // 기존 리뷰 확인
        if (brokerReviewRepository.findByProductIdAndBrokerId(requestDto.getProductId(), requestDto.getBrokerId()).isPresent()) {
            throw new IllegalArgumentException("이미 해당 상품에 대한 리뷰가 존재합니다");
        }

        // 리뷰 생성
        BrokerReview review = BrokerReview.builder()
                .product(product)
                .broker(broker)
                .reviewStatus(requestDto.getReviewStatus())
                .reviewComment(requestDto.getReviewComment())
                .suggestedHsCode(requestDto.getSuggestedHsCode())
                .requestedAt(LocalDateTime.now())
                .build();

        BrokerReview savedReview = brokerReviewRepository.save(review);
        log.info("리뷰 요청 생성 완료 - 리뷰 ID: {}", savedReview.getId());

        return convertToResponseDto(savedReview);
    }

    @Override
    public BrokerReviewResponseDto updateReviewStatus(Integer reviewId, ReviewStatus reviewStatus, 
                                                      String reviewComment, String suggestedHsCode) {
        log.info("리뷰 상태 업데이트 - 리뷰 ID: {}, 상태: {}", reviewId, reviewStatus);

        BrokerReview review = brokerReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다: " + reviewId));

        review.setReviewStatus(reviewStatus);
        review.setReviewComment(reviewComment);
        review.setSuggestedHsCode(suggestedHsCode);
        review.setReviewedAt(LocalDateTime.now());

        BrokerReview savedReview = brokerReviewRepository.save(review);
        log.info("리뷰 상태 업데이트 완료 - 리뷰 ID: {}", savedReview.getId());

        return convertToResponseDto(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public BrokerReviewResponseDto getReviewById(Integer reviewId) {
        log.info("리뷰 상세 조회 - 리뷰 ID: {}", reviewId);

        BrokerReview review = brokerReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다: " + reviewId));

        return convertToResponseDto(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BrokerReviewListResponseDto> getReviewsByBrokerId(Integer brokerId, Pageable pageable) {
        log.info("관세사별 리뷰 목록 조회 - 관세사 ID: {}", brokerId);

        Page<BrokerReview> reviews = brokerReviewRepository.findByBrokerIdOrderByCreatedAtDesc(brokerId, pageable);
        return reviews.map(this::convertToListResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BrokerReviewListResponseDto> getReviewsByProductId(Integer productId, Pageable pageable) {
        log.info("상품별 리뷰 목록 조회 - 상품 ID: {}", productId);

        Page<BrokerReview> reviews = brokerReviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
        return reviews.map(this::convertToListResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BrokerReviewListResponseDto> getReviewsByStatus(ReviewStatus reviewStatus, Pageable pageable) {
        log.info("리뷰 상태별 목록 조회 - 상태: {}", reviewStatus);

        Page<BrokerReview> reviews = brokerReviewRepository.findByReviewStatusOrderByCreatedAtDesc(reviewStatus, pageable);
        return reviews.map(this::convertToListResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrokerReviewListResponseDto> getPendingReviewsByBrokerId(Integer brokerId) {
        log.info("관세사별 대기 중인 리뷰 목록 조회 - 관세사 ID: {}", brokerId);

        List<BrokerReview> reviews = brokerReviewRepository.findPendingReviewsByBrokerId(brokerId);
        return reviews.stream()
                .map(this::convertToListResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BrokerReviewListResponseDto> getLatestReviewsByProductId(Integer productId, Pageable pageable) {
        log.info("상품별 최신 리뷰 조회 - 상품 ID: {}", productId);

        Page<BrokerReview> reviews = brokerReviewRepository.findLatestReviewsByProductId(productId, pageable);
        return reviews.map(this::convertToListResponseDto);
    }

    @Override
    public void deleteReview(Integer reviewId, Integer brokerId) {
        log.info("리뷰 삭제 - 리뷰 ID: {}, 관세사 ID: {}", reviewId, brokerId);

        BrokerReview review = brokerReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다: " + reviewId));

        // 권한 확인
        if (!review.getBroker().getId().equals(brokerId)) {
            throw new IllegalArgumentException("리뷰 삭제 권한이 없습니다");
        }

        brokerReviewRepository.delete(review);
        log.info("리뷰 삭제 완료 - 리뷰 ID: {}", reviewId);
    }

    /**
     * BrokerReview 엔티티를 BrokerReviewResponseDto로 변환
     */
    private BrokerReviewResponseDto convertToResponseDto(BrokerReview review) {
        return BrokerReviewResponseDto.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getProductName())
                .brokerId(review.getBroker().getId())
                .brokerName(review.getBroker().getUserName())
                .reviewStatus(review.getReviewStatus())
                .reviewComment(review.getReviewComment())
                .suggestedHsCode(review.getSuggestedHsCode())
                .requestedAt(review.getRequestedAt())
                .reviewedAt(review.getReviewedAt())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    /**
     * BrokerReview 엔티티를 BrokerReviewListResponseDto로 변환
     */
    private BrokerReviewListResponseDto convertToListResponseDto(BrokerReview review) {
        return BrokerReviewListResponseDto.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getProductName())
                .brokerId(review.getBroker().getId())
                .brokerName(review.getBroker().getUserName())
                .reviewStatus(review.getReviewStatus())
                .reviewComment(review.getReviewComment())
                .requestedAt(review.getRequestedAt())
                .reviewedAt(review.getReviewedAt())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
