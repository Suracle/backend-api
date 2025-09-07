package com.suracle.backend_api.dto.analysis;

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
public class HsCodeAnalysisResponseDto {
    
    private String analysisSessionId;
    private List<HsCodeSuggestionDto> suggestions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HsCodeSuggestionDto {
        private Integer id;
        private String hsCode;
        private String description;
        private BigDecimal confidenceScore;
        private String reasoning;
        private BigDecimal usTariffRate;
    }
}
