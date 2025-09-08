package com.suracle.backend_api.dto.inquiry;

import com.suracle.backend_api.entity.inquiry.enums.InquiryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInquiryResponseDto {

    private Integer id;
    private Integer userId;
    private String userName;
    private Integer productId;
    private String productName;
    private InquiryType inquiryType;
    private String inquiryData;
    private String aiResponse;
    private String responseSources;
    private Boolean fromCache;
    private Integer responseTimeMs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
