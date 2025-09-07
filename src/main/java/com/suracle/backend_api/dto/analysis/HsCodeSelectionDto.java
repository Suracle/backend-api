package com.suracle.backend_api.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HsCodeSelectionDto {
    
    private Integer analysisId;
    private String analysisSessionId;
}
