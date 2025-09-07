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
    private List<String> criticalActions;
    private List<String> requiredDocuments;
    private List<String> complianceSteps;
    private String timeline;
    private String brokerRejectionReason;
    private String criticalDeadline;
    private String qualityStandards;
    private String coldChainRequirement;
    private String criticalWarning;
    private String pendingAnalysis;
    private List<String> sources;
    private double confidenceScore;
    private boolean isValid;
    private String lastUpdated;
}
