package com.suracle.backend_api.service.impl;

import com.suracle.backend_api.dto.analysis.HsCodeAnalysisRequestDto;
import com.suracle.backend_api.dto.analysis.HsCodeAnalysisResponseDto;
import com.suracle.backend_api.dto.analysis.HsCodeSelectionDto;
import com.suracle.backend_api.entity.hs.HsCodeAnalysis;
import com.suracle.backend_api.repository.HsCodeAnalysisRepository;
import com.suracle.backend_api.service.HsCodeAnalysisService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class HsCodeAnalysisServiceImpl implements HsCodeAnalysisService {

    private final HsCodeAnalysisRepository hsCodeAnalysisRepository;
    
    @Value("${ai.engine.url:http://localhost:8000}")
    private String aiEngineUrl;
    
    private final RestTemplate restTemplate;
    
    public HsCodeAnalysisServiceImpl(HsCodeAnalysisRepository hsCodeAnalysisRepository) {
        this.hsCodeAnalysisRepository = hsCodeAnalysisRepository;
        
        // RestTemplate 설정 (180초 타임아웃 - LLM + 판례 검색 고려)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(180));
        factory.setReadTimeout(Duration.ofSeconds(180));
        
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public HsCodeAnalysisResponseDto analyzeHsCode(HsCodeAnalysisRequestDto request) {
        log.info("📥 HS코드 분석 요청 - 상품명: {}, 세션ID: {}", request.getProductName(), request.getAnalysisSessionId());
        
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
        
        try {
            // AI 엔진 호출
            HsCodeAnalysisResponseDto aiResponse = callAiEngine(request);
            
            // AI 응답을 DB에 저장
            List<HsCodeAnalysis> analyses = convertAiResponseToEntities(aiResponse, request, sessionId);
            hsCodeAnalysisRepository.saveAll(analyses);
            
            // AI 응답을 직접 사용 (변환 불필요)
            return HsCodeAnalysisResponseDto.builder()
                .analysisSessionId(sessionId)
                .suggestions(aiResponse.getSuggestions())
                .processingTime(aiResponse.getProcessingTime())
                .build();
                
        } catch (Exception e) {
            log.error("❌ AI 엔진 호출 실패: {}", e.getMessage());
            throw new RuntimeException("HS코드 분석에 실패했습니다. AI 엔진이 응답하지 않습니다.", e);
        }
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
     * AI 엔진 호출
     */
    private HsCodeAnalysisResponseDto callAiEngine(HsCodeAnalysisRequestDto request) {
        String url = aiEngineUrl + "/api/hs-code/analyze-graph";
        
        log.info("🚀 AI 엔진 호출 - URL: {}", url);
        
        try {
            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 요청 엔티티 생성
            HttpEntity<HsCodeAnalysisRequestDto> entity = new HttpEntity<>(request, headers);
            
            // AI 엔진 호출
            ResponseEntity<HsCodeAnalysisResponseDto> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                HsCodeAnalysisResponseDto.class
            );
            
            HsCodeAnalysisResponseDto responseBody = response.getBody();
            
            if (responseBody != null) {
                log.info("✅ AI 엔진 응답 성공 - 세션ID: {}, 추천 개수: {}", 
                        responseBody.getAnalysisSessionId(),
                        responseBody.getSuggestions() != null ? responseBody.getSuggestions().size() : 0);
            }
            
            return responseBody;
            
        } catch (Exception e) {
            log.error("❌ AI 엔진 호출 실패: {}", e.getMessage());
            throw new RuntimeException("AI 엔진 호출 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * AI 응답을 엔티티로 변환
     */
    private List<HsCodeAnalysis> convertAiResponseToEntities(HsCodeAnalysisResponseDto aiResponse, HsCodeAnalysisRequestDto request, String sessionId) {
        if (aiResponse.getSuggestions() == null) {
            return List.of();
        }
        
        return aiResponse.getSuggestions().stream()
            .map(suggestion -> HsCodeAnalysis.builder()
                .productName(request.getProductName())
                .productDescription(request.getProductDescription())
                .suggestedHsCode(suggestion.getHsCode())
                .hsCodeDescription(suggestion.getDescription())
                .confidenceScore(suggestion.getConfidenceScore())
                .reasoning(suggestion.getReasoning())
                .usTariffRate(suggestion.getUsTariffRate())
                .isSelected(false)
                .analysisSessionId(sessionId)
                .build())
            .collect(Collectors.toList());
    }
    
    
    /**
     * HsCodeAnalysis 엔티티를 HsCodeSuggestionDto로 변환
     */
    private HsCodeAnalysisResponseDto.HsCodeSuggestionDto convertToSuggestionDto(HsCodeAnalysis analysis) {
        // USITC URL 생성
        String hsCodeForUrl = analysis.getSuggestedHsCode().replace(".", "");
        String usitcUrl = "https://hts.usitc.gov/search?query=" + hsCodeForUrl;
        
        return HsCodeAnalysisResponseDto.HsCodeSuggestionDto.builder()
            .id(analysis.getId())
            .hsCode(analysis.getSuggestedHsCode())
            .description(analysis.getHsCodeDescription())
            .confidenceScore(analysis.getConfidenceScore())
            .reasoning(analysis.getReasoning())
            .usTariffRate(analysis.getUsTariffRate())
            .baseTariffRate(analysis.getUsTariffRate()) // 기본 관세율 (현재는 동일)
            .reciprocalTariffRate(BigDecimal.ZERO) // 상호관세율 (기본값 0)
            .usitcUrl(usitcUrl)
            // hierarchicalDescription은 AI 엔진에서 제공되지 않으므로 null
            .hierarchicalDescription(null)
            .build();
    }
    
}
