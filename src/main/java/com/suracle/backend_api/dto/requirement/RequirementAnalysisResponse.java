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
    private List<Object> criticalActions;  // 객체 또는 문자열 지원
    private List<Object> requiredDocuments;  // 객체 또는 문자열 지원
    private List<Object> complianceSteps;  // 객체 또는 문자열 지원
    private Object timeline;  // 문자열 또는 객체 지원
    private String brokerRejectionReason;
    private String criticalDeadline;
    private String qualityStandards;
    private String coldChainRequirement;
    private String criticalWarning;
    private String pendingAnalysis;
    private List<Object> sources;  // 객체 또는 문자열 지원
    private double confidenceScore;
    private boolean isValid;
    private String lastUpdated;
}
