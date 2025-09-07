package com.suracle.backend_api.dto.precedents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrecedentsResponseDto {
    private List<String> successCases;
    private List<String> failureCases;
    private List<String> actionableInsights;
    private List<String> riskFactors;
    private String recommendedAction;
    private Double confidenceScore;
    private Boolean isValid;
}
