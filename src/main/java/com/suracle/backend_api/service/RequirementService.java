package com.suracle.backend_api.service;

import com.suracle.backend_api.dto.requirement.RequirementAnalysisResponse;

public interface RequirementService {
    RequirementAnalysisResponse getRequirementAnalysis(Long productId);
}
