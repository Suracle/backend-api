package com.suracle.backend_api.dto.broker;

import com.suracle.backend_api.entity.broker.enums.ReviewStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrokerReviewRequestDto {

    @NotNull(message = "상품 ID는 필수입니다")
    private Integer productId;

    @NotNull(message = "관세사 ID는 필수입니다")
    private Integer brokerId;

    @NotNull(message = "리뷰 상태는 필수입니다")
    private ReviewStatus reviewStatus;

    @Size(max = 1000, message = "리뷰 코멘트는 1000자를 초과할 수 없습니다")
    private String reviewComment;

    @Size(max = 20, message = "제안된 HS코드는 20자를 초과할 수 없습니다")
    private String suggestedHsCode;
}
