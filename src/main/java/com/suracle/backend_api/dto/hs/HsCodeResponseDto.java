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
    private LocalDateTime lastUpdated;
}
