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
    private List<String> similarProducts;
    private Double approvalRate;
    private List<String> commonIssues;
    private List<String> successFactors;
    private Double confidenceScore;
    private Boolean isValid;
}
