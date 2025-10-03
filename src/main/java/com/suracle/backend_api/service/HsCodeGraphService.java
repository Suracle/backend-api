package com.suracle.backend_api.service;

import com.suracle.backend_api.dto.analysis.HsCodeAnalysisRequestDto;
import com.suracle.backend_api.dto.analysis.HsCodeAnalysisResponseDto;

public interface HsCodeGraphService {
  HsCodeAnalysisResponseDto analyzeViaFastApi(HsCodeAnalysisRequestDto request);
}
