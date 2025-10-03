package com.suracle.backend_api.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.suracle.backend_api.dto.analysis.HsCodeAnalysisRequestDto;
import com.suracle.backend_api.dto.analysis.HsCodeAnalysisResponseDto;
import com.suracle.backend_api.service.HsCodeGraphService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class HsCodeGraphServiceImpl implements HsCodeGraphService {

  private final RestTemplate rtb;

  private final String fastApiBase = System.getenv().getOrDefault("SERVICES_FASTAPI_GRAPH_BASE",
      "http://localhost:8000");

  @Override
  public HsCodeAnalysisResponseDto analyzeViaFastApi(HsCodeAnalysisRequestDto req) {
    String url = fastApiBase + "/api/hs-code/analyze-graph";
    ResponseEntity<Map> res = rtb.postForEntity(url, req, Map.class);
    if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
      throw new IllegalStateException("FastAPI analyze-graph failed: " + res.getStatusCode());
    }
    Map<String, Object> body = (Map<String, Object>) res.getBody();
    String sessionId = Objects.toString(body.get("analysisSessionId"), UUID.randomUUID().toString());
    List<Map<String, Object>> raw = (List<Map<String, Object>>) body.getOrDefault("suggestions", List.of());

    AtomicInteger idx = new AtomicInteger(1);
    List<HsCodeAnalysisResponseDto.HsCodeSuggestionDto> suggestions = raw.stream()
        .map(s -> HsCodeAnalysisResponseDto.HsCodeSuggestionDto.builder()
            .id(idx.getAndIncrement())
            .hsCode(Objects.toString(s.get("hsCode"), ""))
            .description(Objects.toString(s.get("description"), ""))
            .confidenceScore(new BigDecimal(Objects.toString(s.getOrDefault("confidenceScore", "0.0"))))
            .reasoning(Objects.toString(s.get("reasoning"), ""))
            .usTariffRate(new BigDecimal(Objects.toString(s.getOrDefault("usTariffRate", "0.0"))))
            .build())
        .toList();

    return HsCodeAnalysisResponseDto.builder()
        .analysisSessionId(sessionId)
        .suggestions(suggestions)
        .build();
  }
}
