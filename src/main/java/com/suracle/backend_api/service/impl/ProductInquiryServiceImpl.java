package com.suracle.backend_api.service.impl;

import com.suracle.backend_api.dto.inquiry.ProductInquiryRequestDto;
import com.suracle.backend_api.dto.inquiry.ProductInquiryResponseDto;
import com.suracle.backend_api.entity.inquiry.ProductInquiry;
import com.suracle.backend_api.entity.product.Product;
import com.suracle.backend_api.entity.user.User;
import com.suracle.backend_api.repository.ProductInquiryRepository;
import com.suracle.backend_api.repository.ProductRepository;
import com.suracle.backend_api.repository.UserRepository;
import com.suracle.backend_api.service.ProductInquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductInquiryServiceImpl implements ProductInquiryService {

    private final ProductInquiryRepository productInquiryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public ProductInquiryResponseDto createInquiry(ProductInquiryRequestDto requestDto) {
        log.info("문의 생성 - 사용자 ID: {}, 상품 ID: {}, 문의 유형: {}", 
                requestDto.getUserId(), requestDto.getProductId(), requestDto.getInquiryType());

        // 사용자 존재 확인
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + requestDto.getUserId()));

        // 상품 존재 확인 (상품 ID가 있는 경우)
        Product product = null;
        if (requestDto.getProductId() != null) {
            product = productRepository.findById(requestDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + requestDto.getProductId()));
        }

        // 문의 생성
        ProductInquiry inquiry = ProductInquiry.builder()
                .user(user)
                .product(product)
                .inquiryType(requestDto.getInquiryType())
                .inquiryData(requestDto.getInquiryData())
                .aiResponse(requestDto.getAiResponse())
                .responseSources(requestDto.getResponseSources())
                .fromCache(requestDto.getFromCache())
                .responseTimeMs(requestDto.getResponseTimeMs())
                .build();

        ProductInquiry savedInquiry = productInquiryRepository.save(inquiry);
        log.info("문의 생성 완료 - 문의 ID: {}", savedInquiry.getId());

        return convertToResponseDto(savedInquiry);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductInquiryResponseDto getInquiryById(Integer inquiryId) {
        log.info("문의 상세 조회 - 문의 ID: {}", inquiryId);

        ProductInquiry inquiry = productInquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다: " + inquiryId));

        return convertToResponseDto(inquiry);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductInquiryResponseDto> getInquiriesByUserId(Integer userId, Pageable pageable) {
        log.info("사용자별 문의 목록 조회 - 사용자 ID: {}", userId);

        Page<ProductInquiry> inquiries = productInquiryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return inquiries.map(this::convertToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductInquiryResponseDto> getInquiriesByProductId(Integer productId, Pageable pageable) {
        log.info("상품별 문의 목록 조회 - 상품 ID: {}", productId);

        Page<ProductInquiry> inquiries = productInquiryRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
        return inquiries.map(this::convertToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductInquiryResponseDto> getInquiriesByType(com.suracle.backend_api.entity.inquiry.enums.InquiryType inquiryType, Pageable pageable) {
        log.info("문의 유형별 목록 조회 - 문의 유형: {}", inquiryType);

        Page<ProductInquiry> inquiries = productInquiryRepository.findByInquiryTypeOrderByCreatedAtDesc(inquiryType, pageable);
        return inquiries.map(this::convertToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductInquiryResponseDto> getInquiriesByUserIdAndType(Integer userId, com.suracle.backend_api.entity.inquiry.enums.InquiryType inquiryType, Pageable pageable) {
        log.info("사용자별 문의 유형별 조회 - 사용자 ID: {}, 문의 유형: {}", userId, inquiryType);

        Page<ProductInquiry> inquiries = productInquiryRepository.findByUserIdAndInquiryTypeOrderByCreatedAtDesc(userId, inquiryType, pageable);
        return inquiries.map(this::convertToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductInquiryResponseDto> getRecentInquiries(Pageable pageable) {
        log.info("최근 문의 조회");

        Page<ProductInquiry> inquiries = productInquiryRepository.findRecentInquiries(pageable);
        return inquiries.map(this::convertToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductInquiryResponseDto> getInquiriesWithAiResponse(Pageable pageable) {
        log.info("AI 응답이 있는 문의 조회");

        Page<ProductInquiry> inquiries = productInquiryRepository.findInquiriesWithAiResponse(pageable);
        return inquiries.map(this::convertToResponseDto);
    }

    @Override
    public void deleteInquiry(Integer inquiryId, Integer userId) {
        log.info("문의 삭제 - 문의 ID: {}, 사용자 ID: {}", inquiryId, userId);

        ProductInquiry inquiry = productInquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다: " + inquiryId));

        // 권한 확인
        if (!inquiry.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("문의 삭제 권한이 없습니다");
        }

        productInquiryRepository.delete(inquiry);
        log.info("문의 삭제 완료 - 문의 ID: {}", inquiryId);
    }

    /**
     * ProductInquiry 엔티티를 ProductInquiryResponseDto로 변환
     */
    private ProductInquiryResponseDto convertToResponseDto(ProductInquiry inquiry) {
        return ProductInquiryResponseDto.builder()
                .id(inquiry.getId())
                .userId(inquiry.getUser().getId())
                .userName(inquiry.getUser().getUserName())
                .productId(inquiry.getProduct() != null ? inquiry.getProduct().getId() : null)
                .productName(inquiry.getProduct() != null ? inquiry.getProduct().getProductName() : null)
                .inquiryType(inquiry.getInquiryType())
                .inquiryData(inquiry.getInquiryData())
                .aiResponse(inquiry.getAiResponse())
                .responseSources(inquiry.getResponseSources())
                .fromCache(inquiry.getFromCache())
                .responseTimeMs(inquiry.getResponseTimeMs())
                .createdAt(inquiry.getCreatedAt())
                .updatedAt(inquiry.getUpdatedAt())
                .build();
    }
}
