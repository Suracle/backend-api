package com.suracle.backend_api.dto.inquiry;

import com.suracle.backend_api.entity.inquiry.enums.InquiryType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInquiryRequestDto {

    @NotNull(message = "사용자 ID는 필수입니다")
    private Integer userId;

    private Integer productId;

    @NotNull(message = "문의 유형은 필수입니다")
    private InquiryType inquiryType;

    private String inquiryData;

    private String aiResponse;

    private String responseSources;

    @Builder.Default
    private Boolean fromCache = false;

    private Integer responseTimeMs;
}
