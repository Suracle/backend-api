package com.suracle.backend_api.service.impl;

import com.suracle.backend_api.dto.analysis.HsCodeAnalysisRequestDto;
import com.suracle.backend_api.dto.analysis.HsCodeAnalysisResponseDto;
import com.suracle.backend_api.dto.analysis.HsCodeSelectionDto;
import com.suracle.backend_api.entity.hs.HsCodeAnalysis;
import com.suracle.backend_api.repository.HsCodeAnalysisRepository;
import com.suracle.backend_api.service.HsCodeAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HsCodeAnalysisServiceImpl implements HsCodeAnalysisService {

    private final HsCodeAnalysisRepository hsCodeAnalysisRepository;

    @Override
    public HsCodeAnalysisResponseDto analyzeHsCode(HsCodeAnalysisRequestDto request) {
        log.info("HS코드 분석 요청 - 상품명: {}, 세션ID: {}", request.getProductName(), request.getAnalysisSessionId());
        
        // 세션 ID가 없으면 새로 생성
        String sessionId = request.getAnalysisSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        
        // 기존 분석 결과 삭제 (같은 세션의 이전 결과)
        List<HsCodeAnalysis> existingAnalyses = hsCodeAnalysisRepository
            .findByAnalysisSessionIdOrderByConfidenceScoreDesc(sessionId);
        if (!existingAnalyses.isEmpty()) {
            hsCodeAnalysisRepository.deleteAll(existingAnalyses);
        }
        
        // TODO: AI 분석 로직 구현 (현재는 더미 데이터)
        List<HsCodeAnalysis> analyses = createDummyAnalysisResults(request, sessionId);
        
        // 분석 결과 저장
        List<HsCodeAnalysis> savedAnalyses = hsCodeAnalysisRepository.saveAll(analyses);
        
        // 응답 DTO 생성
        List<HsCodeAnalysisResponseDto.HsCodeSuggestionDto> suggestions = savedAnalyses.stream()
            .map(this::convertToSuggestionDto)
            .collect(Collectors.toList());
        
        return HsCodeAnalysisResponseDto.builder()
            .analysisSessionId(sessionId)
            .suggestions(suggestions)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HsCodeAnalysisResponseDto getAnalysisResults(String sessionId) {
        log.info("HS코드 분석 결과 조회 - 세션ID: {}", sessionId);
        
        List<HsCodeAnalysis> analyses = hsCodeAnalysisRepository
            .findByAnalysisSessionIdOrderByConfidenceScoreDesc(sessionId);
        
        List<HsCodeAnalysisResponseDto.HsCodeSuggestionDto> suggestions = analyses.stream()
            .map(this::convertToSuggestionDto)
            .collect(Collectors.toList());
        
        return HsCodeAnalysisResponseDto.builder()
            .analysisSessionId(sessionId)
            .suggestions(suggestions)
            .build();
    }

    @Override
    public HsCodeAnalysisResponseDto.HsCodeSuggestionDto selectHsCode(HsCodeSelectionDto selection) {
        log.info("HS코드 선택 - 분석ID: {}, 세션ID: {}", selection.getAnalysisId(), selection.getAnalysisSessionId());
        
        // 해당 세션의 모든 분석 결과에서 선택 해제
        List<HsCodeAnalysis> allAnalyses = hsCodeAnalysisRepository
            .findByAnalysisSessionIdOrderByConfidenceScoreDesc(selection.getAnalysisSessionId());
        allAnalyses.forEach(analysis -> analysis.setIsSelected(false));
        
        // 선택된 분석 결과만 선택으로 표시
        HsCodeAnalysis selectedAnalysis = hsCodeAnalysisRepository.findById(selection.getAnalysisId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 분석 결과입니다: " + selection.getAnalysisId()));
        
        selectedAnalysis.setIsSelected(true);
        hsCodeAnalysisRepository.save(selectedAnalysis);
        
        return convertToSuggestionDto(selectedAnalysis);
    }
    
    /**
     * 더미 분석 결과 생성 (AI 연동 전 임시 구현)
     */
    private List<HsCodeAnalysis> createDummyAnalysisResults(HsCodeAnalysisRequestDto request, String sessionId) {
        // 실제로는 AI 서비스를 호출하여 분석하지만, 현재는 더미 데이터 생성
        return List.of(
            HsCodeAnalysis.builder()
                .productName(request.getProductName())
                .productDescription(request.getProductDescription())
                .suggestedHsCode("3304.99.50.00")
                .hsCodeDescription("Other beauty or makeup preparations and preparations for the care of the skin (serums, creams)")
                .confidenceScore(new BigDecimal("0.95"))
                .reasoning("상품명과 설명을 분석한 결과, 화장품/스킨케어 제품으로 분류됩니다. 비타민C 세럼은 피부 관리용 화장품에 해당하여 3304.99.50.00 코드가 가장 적합합니다.")
                .usTariffRate(new BigDecimal("0.0000"))
                .isSelected(false)
                .analysisSessionId(sessionId)
                .build(),
            HsCodeAnalysis.builder()
                .productName(request.getProductName())
                .productDescription(request.getProductDescription())
                .suggestedHsCode("3304.99.00.00")
                .hsCodeDescription("Beauty or make-up preparations and preparations for the care of the skin (other than medicaments), including sunscreen or sun tan preparations; manicure or pedicure preparations")
                .confidenceScore(new BigDecimal("0.78"))
                .reasoning("기타 화장품/스킨케어 제품으로 분류될 가능성이 있습니다. 세부 분류가 명확하지 않은 경우 이 코드를 사용할 수 있습니다.")
                .usTariffRate(new BigDecimal("0.0000"))
                .isSelected(false)
                .analysisSessionId(sessionId)
                .build(),
            HsCodeAnalysis.builder()
                .productName(request.getProductName())
                .productDescription(request.getProductDescription())
                .suggestedHsCode("2106.90.00.00")
                .hsCodeDescription("Food preparations not elsewhere specified or included")
                .confidenceScore(new BigDecimal("0.45"))
                .reasoning("식품 관련 제품으로 분류될 가능성이 낮지만, 비타민C가 함유된 경우 식품 보조제로 분류될 수 있습니다.")
                .usTariffRate(new BigDecimal("0.0000"))
                .isSelected(false)
                .analysisSessionId(sessionId)
                .build()
        );
    }
    
    /**
     * HsCodeAnalysis 엔티티를 HsCodeSuggestionDto로 변환
     */
    private HsCodeAnalysisResponseDto.HsCodeSuggestionDto convertToSuggestionDto(HsCodeAnalysis analysis) {
        return HsCodeAnalysisResponseDto.HsCodeSuggestionDto.builder()
            .id(analysis.getId())
            .hsCode(analysis.getSuggestedHsCode())
            .description(analysis.getHsCodeDescription())
            .confidenceScore(analysis.getConfidenceScore())
            .reasoning(analysis.getReasoning())
            .usTariffRate(analysis.getUsTariffRate())
            .build();
    }
}
