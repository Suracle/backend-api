package com.suracle.backend_api.dto.requirement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequirementAnalysisResponse {
    private String productId;
    private String productName;
    private String hsCode;
    private boolean fdaRegistration;
    private boolean cosmeticFacilityRegistration;
    private boolean ingredientSafety;
    private boolean labelingCompliance;
    private boolean colorAdditiveApproval;
    private boolean safetyTesting;
    private boolean phTesting;
    private boolean sensitiveSkinTesting;
    private boolean uvSafety;
    private boolean chemicalDisclosure;
    private boolean proteinComplexDisclosure;
    private boolean aminoAcidDocumentation;
    private boolean hairStrengthClaims;
    private boolean snailExtractSafety;
    private boolean sulfateFreeClaim;
    private List<String> additionalDocs;
    private List<String> sources;
    private double confidenceScore;
    private boolean isValid;
    private String lastUpdated;
}
