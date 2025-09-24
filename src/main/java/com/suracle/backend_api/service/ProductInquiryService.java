package com.suracle.backend_api.service;

import com.suracle.backend_api.dto.inquiry.ProductInquiryRequestDto;
import com.suracle.backend_api.dto.inquiry.ProductInquiryResponseDto;
import com.suracle.backend_api.entity.inquiry.enums.InquiryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductInquiryService {

    /**
     * 문의 생성
     * @param requestDto 문의 요청 정보
     * @return 생성된 문의 정보
     */
    ProductInquiryResponseDto createInquiry(ProductInquiryRequestDto requestDto);

    /**
     * 문의 상세 조회
     * @param inquiryId 문의 ID
     * @return 문의 상세 정보
     */
    ProductInquiryResponseDto getInquiryById(Integer inquiryId);

    /**
     * 사용자별 문의 목록 조회
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 사용자 문의 목록
     */
    Page<ProductInquiryResponseDto> getInquiriesByUserId(Integer userId, Pageable pageable);

    /**
     * 상품별 문의 목록 조회
     * @param productId 상품 ID
     * @param pageable 페이징 정보
     * @return 상품 문의 목록
     */
    Page<ProductInquiryResponseDto> getInquiriesByProductId(Integer productId, Pageable pageable);

    /**
     * 문의 유형별 목록 조회
     * @param inquiryType 문의 유형
     * @param pageable 페이징 정보
     * @return 문의 목록
     */
    Page<ProductInquiryResponseDto> getInquiriesByType(InquiryType inquiryType, Pageable pageable);

    /**
     * 사용자별 문의 유형별 조회
     * @param userId 사용자 ID
     * @param inquiryType 문의 유형
     * @param pageable 페이징 정보
     * @return 문의 목록
     */
    Page<ProductInquiryResponseDto> getInquiriesByUserIdAndType(Integer userId, InquiryType inquiryType, Pageable pageable);

    /**
     * 최근 문의 조회
     * @param pageable 페이징 정보
     * @return 최근 문의 목록
     */
    Page<ProductInquiryResponseDto> getRecentInquiries(Pageable pageable);

    /**
     * AI 응답이 있는 문의 조회
     * @param pageable 페이징 정보
     * @return AI 응답 문의 목록
     */
    Page<ProductInquiryResponseDto> getInquiriesWithAiResponse(Pageable pageable);

    /**
     * 문의 삭제
     * @param inquiryId 문의 ID
     * @param userId 사용자 ID (권한 확인용)
     */
    void deleteInquiry(Integer inquiryId, Integer userId);
}
