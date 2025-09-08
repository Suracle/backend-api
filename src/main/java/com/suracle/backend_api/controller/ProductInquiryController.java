package com.suracle.backend_api.controller;

import com.suracle.backend_api.dto.inquiry.ProductInquiryRequestDto;
import com.suracle.backend_api.dto.inquiry.ProductInquiryResponseDto;
import com.suracle.backend_api.entity.inquiry.enums.InquiryType;
import com.suracle.backend_api.service.ProductInquiryService;
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

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
@Slf4j
public class ProductInquiryController {

    private final ProductInquiryService productInquiryService;

    /**
     * 문의 생성
     * @param requestDto 문의 요청 정보
     * @return 생성된 문의 정보
     */
    @PostMapping
    public ResponseEntity<ProductInquiryResponseDto> createInquiry(
            @Valid @RequestBody ProductInquiryRequestDto requestDto) {
        try {
            log.info("문의 생성 API 호출 - 사용자 ID: {}, 상품 ID: {}", 
                    requestDto.getUserId(), requestDto.getProductId());
            
            ProductInquiryResponseDto response = productInquiryService.createInquiry(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("문의 생성 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("문의 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 문의 상세 조회
     * @param inquiryId 문의 ID
     * @return 문의 상세 정보
     */
    @GetMapping("/{inquiryId}")
    public ResponseEntity<ProductInquiryResponseDto> getInquiryById(@PathVariable Integer inquiryId) {
        try {
            log.info("문의 상세 조회 API 호출 - 문의 ID: {}", inquiryId);
            
            ProductInquiryResponseDto response = productInquiryService.getInquiryById(inquiryId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("문의 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("문의 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 사용자별 문의 목록 조회
     * @param userId 사용자 ID
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 사용자 문의 목록
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ProductInquiryResponseDto>> getInquiriesByUserId(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("사용자별 문의 목록 조회 API 호출 - 사용자 ID: {}, 페이지: {}", userId, page);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ProductInquiryResponseDto> response = productInquiryService.getInquiriesByUserId(userId, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자별 문의 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 상품별 문의 목록 조회
     * @param productId 상품 ID
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 상품 문의 목록
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ProductInquiryResponseDto>> getInquiriesByProductId(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("상품별 문의 목록 조회 API 호출 - 상품 ID: {}, 페이지: {}", productId, page);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ProductInquiryResponseDto> response = productInquiryService.getInquiriesByProductId(productId, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("상품별 문의 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 문의 유형별 목록 조회
     * @param inquiryType 문의 유형
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 문의 목록
     */
    @GetMapping("/type/{inquiryType}")
    public ResponseEntity<Page<ProductInquiryResponseDto>> getInquiriesByType(
            @PathVariable InquiryType inquiryType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("문의 유형별 목록 조회 API 호출 - 문의 유형: {}, 페이지: {}", inquiryType, page);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ProductInquiryResponseDto> response = productInquiryService.getInquiriesByType(inquiryType, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("문의 유형별 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 사용자별 문의 유형별 조회
     * @param userId 사용자 ID
     * @param inquiryType 문의 유형
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 문의 목록
     */
    @GetMapping("/user/{userId}/type/{inquiryType}")
    public ResponseEntity<Page<ProductInquiryResponseDto>> getInquiriesByUserIdAndType(
            @PathVariable Integer userId,
            @PathVariable InquiryType inquiryType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("사용자별 문의 유형별 조회 API 호출 - 사용자 ID: {}, 문의 유형: {}, 페이지: {}", userId, inquiryType, page);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ProductInquiryResponseDto> response = productInquiryService.getInquiriesByUserIdAndType(userId, inquiryType, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자별 문의 유형별 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 최근 문의 조회
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 최근 문의 목록
     */
    @GetMapping("/recent")
    public ResponseEntity<Page<ProductInquiryResponseDto>> getRecentInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("최근 문의 조회 API 호출 - 페이지: {}", page);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ProductInquiryResponseDto> response = productInquiryService.getRecentInquiries(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("최근 문의 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * AI 응답이 있는 문의 조회
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return AI 응답 문의 목록
     */
    @GetMapping("/ai-responses")
    public ResponseEntity<Page<ProductInquiryResponseDto>> getInquiriesWithAiResponse(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("AI 응답이 있는 문의 조회 API 호출 - 페이지: {}", page);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ProductInquiryResponseDto> response = productInquiryService.getInquiriesWithAiResponse(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("AI 응답이 있는 문의 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 문의 삭제
     * @param inquiryId 문의 ID
     * @param userId 사용자 ID (헤더로 전달)
     * @return 삭제 결과
     */
    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<Void> deleteInquiry(
            @PathVariable Integer inquiryId,
            @RequestHeader("X-User-Id") Integer userId) {
        try {
            log.info("문의 삭제 API 호출 - 문의 ID: {}, 사용자 ID: {}", inquiryId, userId);
            
            productInquiryService.deleteInquiry(inquiryId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("문의 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("문의 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
