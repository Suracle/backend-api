package com.suracle.backend_api.controller;

import com.suracle.backend_api.dto.requirement.RequirementAnalysisResponse;
import com.suracle.backend_api.service.RequirementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
@Slf4j
public class RequirementController {

    private final RequirementService requirementService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<RequirementAnalysisResponse> getRequirementAnalysis(
            @PathVariable Long productId) {
        try {
            log.info("Requesting requirement analysis for product ID: {}", productId);
            RequirementAnalysisResponse response = requirementService.getRequirementAnalysis(productId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting requirement analysis for product ID: {}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
