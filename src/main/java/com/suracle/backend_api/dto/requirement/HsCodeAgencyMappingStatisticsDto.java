package com.suracle.backend_api.dto.requirement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HsCodeAgencyMappingStatisticsDto {
    private Long totalMappings;
    private Integer recentMappings;
    private Double averageConfidence;
    private String mostUsedAgency;
}
