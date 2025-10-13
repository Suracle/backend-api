package com.suracle.backend_api.dto.hs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HsCodeResponseDto {
    
    private String hsCode;
    private String description;
    private BigDecimal usTariffRate;
    private String reasoning;
    private String tariffReasoning;  // 관세율 적용 근거 추가
    private LocalDateTime lastUpdated;
}
