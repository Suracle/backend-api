package com.suracle.backend_api.service;

import com.suracle.backend_api.dto.analysis.HsCodeAnalysisRequestDto;
import com.suracle.backend_api.dto.analysis.HsCodeAnalysisResponseDto;
import com.suracle.backend_api.dto.analysis.HsCodeSelectionDto;

public interface HsCodeAnalysisService {
    
    /**
     * 상품명과 설명을 기반으로 HS코드 분석 수행
     * @param request 분석 요청 정보
     * @return HS코드 추천 목록
     */
    HsCodeAnalysisResponseDto analyzeHsCode(HsCodeAnalysisRequestDto request);
    
    /**
     * 분석 세션 ID로 추천된 HS코드 목록 조회
     * @param sessionId 분석 세션 ID
     * @return HS코드 추천 목록
     */
    HsCodeAnalysisResponseDto getAnalysisResults(String sessionId);
    
    /**
     * 사용자가 선택한 HS코드 저장
     * @param selection 선택 정보
     * @return 선택된 HS코드 정보
     */
    HsCodeAnalysisResponseDto.HsCodeSuggestionDto selectHsCode(HsCodeSelectionDto selection);
}
