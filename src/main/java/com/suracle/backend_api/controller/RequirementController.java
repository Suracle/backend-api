package com.suracle.backend_api.controller;

import com.suracle.backend_api.dto.requirement.RequirementAnalysisResponse;
import com.suracle.backend_api.service.RequirementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 요구사항 분석 조회 컨트롤러
 * 
 * <p>이 컨트롤러는 상품 ID를 받아 AI 엔진에서 분석된 요구사항을 반환합니다.
 * 
 * <p>주요 기능:
 * <ul>
 *   <li>GET /api/requirements/product/{productId}: 상품 요구사항 분석 조회</li>
 *   <li>DB 캐시 우선 조회 (ProductAnalysisCache)</li>
 *   <li>캐시 없으면 AI 엔진 호출 (http://localhost:8000)</li>
 * </ul>
 * 
 * <p>AI Engine 엔드포인트: POST /requirements/analyze
 * 
 * @see RequirementService
 * @see com.suracle.backend_api.service.AiWorkflowService
 */
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
