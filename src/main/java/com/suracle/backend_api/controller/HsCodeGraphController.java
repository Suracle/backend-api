package com.suracle.backend_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suracle.backend_api.dto.analysis.HsCodeAnalysisRequestDto;
import com.suracle.backend_api.dto.analysis.HsCodeAnalysisResponseDto;
import com.suracle.backend_api.service.HsCodeGraphService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/hs-code-graph")
@RequiredArgsConstructor
@Slf4j
public class HsCodeGraphController {

  private final HsCodeGraphService service;

  @PostMapping("/analyze")
  public ResponseEntity<HsCodeAnalysisResponseDto> analyze(@RequestBody HsCodeAnalysisRequestDto request) {
    try {
      return ResponseEntity.ok(service.analyzeViaFastApi(request));
    } catch (Exception e) {
      log.error("error", e);
      return ResponseEntity.internalServerError().build();
    }
  }

}
