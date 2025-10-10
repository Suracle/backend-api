package com.suracle.backend_api.dto.requirement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HsCodeAgencyMappingDto {
    
    private Long id;
    private String hsCode;
    private String productCategory;
    private String productDescription;
    private List<String> recommendedAgencies; // List로 변경
    private BigDecimal confidenceScore;
    private Integer usageCount;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
