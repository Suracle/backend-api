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
    private Double processingTime;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HsCodeSuggestionDto {
        private Integer id;
        private String hsCode;
        private String description;
        private BigDecimal confidenceScore;
        private String reasoning;  // HS 코드 추천 근거
        private String tariffReasoning;  // 관세율 적용 근거
        private BigDecimal usTariffRate;
        private BigDecimal baseTariffRate;
        private BigDecimal reciprocalTariffRate;
        private String usitcUrl;
        private HierarchicalDescriptionDto hierarchicalDescription;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HierarchicalDescriptionDto {
        private String heading;
        private String subheading;
        private String tertiary;
        private String combinedDescription;
        private String headingCode;
        private String subheadingCode;
        private String tertiaryCode;
    }
}
