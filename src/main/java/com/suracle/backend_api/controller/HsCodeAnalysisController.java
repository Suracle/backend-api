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
@RequestMapping("/api/hs-code-analysis")
@RequiredArgsConstructor
@Slf4j
public class HsCodeAnalysisController {

    private final HsCodeAnalysisService hsCodeAnalysisService;

    /**
     * 상품명과 설명을 기반으로 HS코드 분석 수행
     * @param request 분석 요청 정보
     * @return HS코드 추천 목록
     */
    @PostMapping("/analyze")
    public ResponseEntity<HsCodeAnalysisResponseDto> analyzeHsCode(@RequestBody HsCodeAnalysisRequestDto request) {
        try {
            log.info("HS코드 분석 요청 - 상품명: {}", request.getProductName());
            HsCodeAnalysisResponseDto response = hsCodeAnalysisService.analyzeHsCode(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("HS코드 분석 중 오류 발생", e);
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
