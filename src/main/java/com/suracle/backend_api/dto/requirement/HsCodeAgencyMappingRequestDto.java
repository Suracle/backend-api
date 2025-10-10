package com.suracle.backend_api.dto.requirement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HsCodeAgencyMappingRequestDto {
    private String hsCode;
    private String productCategory;
    private String productDescription;
    private List<String> recommendedAgencies; // List로 변경
    private BigDecimal confidenceScore;
}
