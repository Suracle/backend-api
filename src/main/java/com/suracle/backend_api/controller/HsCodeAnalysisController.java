package com.suracle.backend_api.controller;

import com.suracle.backend_api.dto.analysis.HsCodeAnalysisRequestDto;
import com.suracle.backend_api.dto.analysis.HsCodeAnalysisResponseDto;
import com.suracle.backend_api.dto.analysis.HsCodeSelectionDto;
import com.suracle.backend_api.service.HsCodeAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HsCodeAnalysisController {

    private final HsCodeAnalysisService hsCodeAnalysisService;

    /**
     * 상품명과 설명을 기반으로 HS코드 분석 수행 (AI 엔진 연동) - 기존 엔드포인트
     * @param request 분석 요청 정보
     * @return HS코드 추천 목록
     */
    @PostMapping("/api/hs-code-analysis/analyze")
    public ResponseEntity<HsCodeAnalysisResponseDto> analyzeHsCode(@RequestBody HsCodeAnalysisRequestDto request) {
        try {
            log.info("📥 HS코드 분석 요청 - 상품명: {}, 설명: {}", 
                    request.getProductName(), 
                    request.getProductDescription() != null ? 
                        request.getProductDescription().substring(0, Math.min(50, request.getProductDescription().length())) + "..." : 
                        "null");
            
            // 입력 검증
            if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {
                log.warn("⚠️ 제품명이 비어있습니다.");
                return ResponseEntity.badRequest().build();
            }
            
            if (request.getProductDescription() == null || request.getProductDescription().trim().isEmpty()) {
                log.warn("⚠️ 제품 설명이 비어있습니다.");
                return ResponseEntity.badRequest().build();
            }
            
            HsCodeAnalysisResponseDto response = hsCodeAnalysisService.analyzeHsCode(request);
            
            log.info("✅ HS코드 분석 완료 - 세션ID: {}, 추천 개수: {}", 
                    response.getAnalysisSessionId(),
                    response.getSuggestions() != null ? response.getSuggestions().size() : 0);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ HS코드 분석 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 상품명과 설명을 기반으로 HS코드 분석 수행 (프론트엔드용 엔드포인트)
     * @param request 분석 요청 정보
     * @return HS코드 추천 목록
     */
    @PostMapping("/api/hs-code-analysis/analyze-graph")
    public ResponseEntity<HsCodeAnalysisResponseDto> analyzeHsCodeGraph(@RequestBody HsCodeAnalysisRequestDto request) {
        try {
            log.info("📥 HS코드 그래프 분석 요청 - 상품명: {}, 설명: {}", 
                    request.getProductName(), 
                    request.getProductDescription() != null ? 
                        request.getProductDescription().substring(0, Math.min(50, request.getProductDescription().length())) + "..." : 
                        "null");
            
            // 입력 검증
            if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {
                log.warn("⚠️ 제품명이 비어있습니다.");
                return ResponseEntity.badRequest().build();
            }
            
            if (request.getProductDescription() == null || request.getProductDescription().trim().isEmpty()) {
                log.warn("⚠️ 제품 설명이 비어있습니다.");
                return ResponseEntity.badRequest().build();
            }
            
            // AI 엔진을 통한 분석 수행 (DB 저장 + 응답 변환 포함)
            HsCodeAnalysisResponseDto response = hsCodeAnalysisService.analyzeHsCode(request);
            
            log.info("✅ HS코드 그래프 분석 완료 - 세션ID: {}, 추천 개수: {}", 
                    response.getAnalysisSessionId(),
                    response.getSuggestions() != null ? response.getSuggestions().size() : 0);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ HS코드 그래프 분석 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 분석 세션 ID로 추천된 HS코드 목록 조회
     * @param sessionId 분석 세션 ID
     * @return HS코드 추천 목록
     */
    @GetMapping("/results/{sessionId}")
    public ResponseEntity<HsCodeAnalysisResponseDto> getAnalysisResults(@PathVariable String sessionId) {
        try {
            log.info("HS코드 분석 결과 조회 - 세션ID: {}", sessionId);
            HsCodeAnalysisResponseDto response = hsCodeAnalysisService.getAnalysisResults(sessionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("HS코드 분석 결과 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 사용자가 선택한 HS코드 저장
     * @param selection 선택 정보
     * @return 선택된 HS코드 정보
     */
    @PostMapping("/select")
    public ResponseEntity<HsCodeAnalysisResponseDto.HsCodeSuggestionDto> selectHsCode(@RequestBody HsCodeSelectionDto selection) {
        try {
            log.info("HS코드 선택 - 분석ID: {}", selection.getAnalysisId());
            HsCodeAnalysisResponseDto.HsCodeSuggestionDto response = hsCodeAnalysisService.selectHsCode(selection);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("HS코드 선택 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("HS코드 선택 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
