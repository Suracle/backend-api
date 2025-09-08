package com.suracle.backend_api.dto.broker;

import com.suracle.backend_api.entity.broker.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrokerReviewResponseDto {

    private Integer id;
    private Integer productId;
    private String productName;
    private Integer brokerId;
    private String brokerName;
    private ReviewStatus reviewStatus;
    private String reviewComment;
    private String suggestedHsCode;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
