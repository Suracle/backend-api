package com.suracle.backend_api.controller;

import com.suracle.backend_api.dto.broker.BrokerReviewListResponseDto;
import com.suracle.backend_api.dto.broker.BrokerReviewRequestDto;
import com.suracle.backend_api.dto.broker.BrokerReviewResponseDto;
import com.suracle.backend_api.entity.broker.enums.ReviewStatus;
import com.suracle.backend_api.service.BrokerReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/broker/reviews")
@RequiredArgsConstructor
@Slf4j
public class BrokerReviewController {

    private final BrokerReviewService brokerReviewService;

    /**
     * 리뷰 요청 생성
     * @param requestDto 리뷰 요청 정보
     * @return 생성된 리뷰 정보
     */
    @PostMapping
    public ResponseEntity<BrokerReviewResponseDto> createReviewRequest(
            @Valid @RequestBody BrokerReviewRequestDto requestDto) {
        try {
            log.info("리뷰 요청 생성 API 호출 - 상품 ID: {}, 관세사 ID: {}", 
                    requestDto.getProductId(), requestDto.getBrokerId());
            
            BrokerReviewResponseDto response = brokerReviewService.createReviewRequest(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("리뷰 요청 생성 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("리뷰 요청 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 리뷰 상태 업데이트 (관세사가 리뷰 처리)
     * @param reviewId 리뷰 ID
     * @param reviewStatus 새로운 리뷰 상태
     * @param reviewComment 리뷰 코멘트
     * @param suggestedHsCode 제안된 HS코드
     * @return 업데이트된 리뷰 정보
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<BrokerReviewResponseDto> updateReviewStatus(
            @PathVariable Integer reviewId,
            @RequestParam ReviewStatus reviewStatus,
            @RequestParam(required = false) String reviewComment,
            @RequestParam(required = false) String suggestedHsCode) {
        try {
            log.info("리뷰 상태 업데이트 API 호출 - 리뷰 ID: {}, 상태: {}", reviewId, reviewStatus);
            
            BrokerReviewResponseDto response = brokerReviewService.updateReviewStatus(
                    reviewId, reviewStatus, reviewComment, suggestedHsCode);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("리뷰 상태 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("리뷰 상태 업데이트 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 리뷰 상세 조회
     * @param reviewId 리뷰 ID
     * @return 리뷰 상세 정보
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<BrokerReviewResponseDto> getReviewById(@PathVariable Integer reviewId) {
        try {
            log.info("리뷰 상세 조회 API 호출 - 리뷰 ID: {}", reviewId);
            
            BrokerReviewResponseDto response = brokerReviewService.getReviewById(reviewId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("리뷰 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("리뷰 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 관세사별 리뷰 목록 조회
     * @param brokerId 관세사 ID
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 관세사 리뷰 목록
     */
    @GetMapping("/broker/{brokerId}")
    public ResponseEntity<Page<BrokerReviewListResponseDto>> getReviewsByBrokerId(
            @PathVariable Integer brokerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("관세사별 리뷰 목록 조회 API 호출 - 관세사 ID: {}, 페이지: {}", brokerId, page);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<BrokerReviewListResponseDto> response = brokerReviewService.getReviewsByBrokerId(brokerId, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("관세사별 리뷰 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 상품별 리뷰 목록 조회
     * @param productId 상품 ID
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 상품 리뷰 목록
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<BrokerReviewListResponseDto>> getReviewsByProductId(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("상품별 리뷰 목록 조회 API 호출 - 상품 ID: {}, 페이지: {}", productId, page);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<BrokerReviewListResponseDto> response = brokerReviewService.getReviewsByProductId(productId, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("상품별 리뷰 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 리뷰 상태별 목록 조회
     * @param status 리뷰 상태
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 리뷰 목록
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<BrokerReviewListResponseDto>> getReviewsByStatus(
            @PathVariable ReviewStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("리뷰 상태별 목록 조회 API 호출 - 상태: {}, 페이지: {}", status, page);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<BrokerReviewListResponseDto> response = brokerReviewService.getReviewsByStatus(status, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("리뷰 상태별 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 관세사별 대기 중인 리뷰 목록 조회
     * @param brokerId 관세사 ID
     * @return 대기 중인 리뷰 목록
     */
    @GetMapping("/broker/{brokerId}/pending")
    public ResponseEntity<List<BrokerReviewListResponseDto>> getPendingReviewsByBrokerId(
            @PathVariable Integer brokerId) {
        try {
            log.info("관세사별 대기 중인 리뷰 목록 조회 API 호출 - 관세사 ID: {}", brokerId);
            
            List<BrokerReviewListResponseDto> response = brokerReviewService.getPendingReviewsByBrokerId(brokerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("관세사별 대기 중인 리뷰 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 상품별 최신 리뷰 조회
     * @param productId 상품 ID
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 최신 리뷰 목록
     */
    @GetMapping("/product/{productId}/latest")
    public ResponseEntity<Page<BrokerReviewListResponseDto>> getLatestReviewsByProductId(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("상품별 최신 리뷰 조회 API 호출 - 상품 ID: {}, 페이지: {}", productId, page);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<BrokerReviewListResponseDto> response = brokerReviewService.getLatestReviewsByProductId(productId, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("상품별 최신 리뷰 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 리뷰 삭제
     * @param reviewId 리뷰 ID
     * @param brokerId 관세사 ID (헤더로 전달)
     * @return 삭제 결과
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Integer reviewId,
            @RequestHeader("X-Broker-Id") Integer brokerId) {
        try {
            log.info("리뷰 삭제 API 호출 - 리뷰 ID: {}, 관세사 ID: {}", reviewId, brokerId);
            
            brokerReviewService.deleteReview(reviewId, brokerId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("리뷰 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("리뷰 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
