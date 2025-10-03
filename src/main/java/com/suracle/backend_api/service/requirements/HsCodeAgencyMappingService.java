package com.suracle.backend_api.service.requirements;

import com.suracle.backend_api.dto.requirement.HsCodeAgencyMappingDto;
import com.suracle.backend_api.dto.requirement.HsCodeAgencyMappingRequestDto;
import com.suracle.backend_api.dto.requirement.HsCodeAgencyMappingStatisticsDto;
import com.suracle.backend_api.entity.requirements.HsCodeAgencyMapping;
import com.suracle.backend_api.repository.requirements.HsCodeAgencyMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HsCodeAgencyMappingService {
    
    private final HsCodeAgencyMappingRepository repository;
    
    /**
     * HS코드로 기관 매핑 조회
     */
    @Transactional(readOnly = true)
    public Optional<HsCodeAgencyMappingDto> findByHsCode(String hsCode) {
        log.info("HS코드 기관 매핑 조회 - HS코드: {}", hsCode);
        
        return repository.findByHsCode(hsCode)
                .map(this::convertToDto);
    }
    
    /**
     * HS코드와 상품명으로 기관 매핑 조회
     */
    @Transactional(readOnly = true)
    public Optional<HsCodeAgencyMappingDto> findByHsCodeAndProduct(String hsCode, String productName) {
        log.info("HS코드 기관 매핑 조회 - HS코드: {}, 상품명: {}", hsCode, productName);
        
        return repository.findByHsCodeAndProductCategory(hsCode, productName)
                .map(this::convertToDto);
    }
    
    /**
     * 기관 매핑 저장
     */
    public HsCodeAgencyMappingDto save(HsCodeAgencyMappingRequestDto request) {
        log.info("HS코드 기관 매핑 저장 - HS코드: {}", request.getHsCode());
        
        // 기존 매핑 확인
        Optional<HsCodeAgencyMapping> existing = repository.findByHsCodeAndProductCategory(
                request.getHsCode(), 
                request.getProductCategory()
        );
        
        HsCodeAgencyMapping mapping;
        if (existing.isPresent()) {
            // 기존 매핑 업데이트
            mapping = existing.get();
            mapping.setRecommendedAgencies(String.join(",", request.getRecommendedAgencies()));
            mapping.setConfidenceScore(request.getConfidenceScore());
            mapping.setProductDescription(request.getProductDescription());
            mapping.setUpdatedAt(LocalDateTime.now());
        } else {
            // 새 매핑 생성
            mapping = HsCodeAgencyMapping.builder()
                    .hsCode(request.getHsCode())
                    .productCategory(request.getProductCategory())
                    .productDescription(request.getProductDescription())
                    .recommendedAgencies(String.join(",", request.getRecommendedAgencies()))
                    .confidenceScore(request.getConfidenceScore())
                    .usageCount(0)
                    .lastUsedAt(LocalDateTime.now())
                    .build();
        }
        
        HsCodeAgencyMapping saved = repository.save(mapping);
        log.info("HS코드 기관 매핑 저장 완료 - ID: {}", saved.getId());
        
        return convertToDto(saved);
    }
    
    /**
     * 사용 횟수 업데이트
     */
    public void updateUsageCount(String hsCode, String productName) {
        log.info("사용 횟수 업데이트 - HS코드: {}, 상품명: {}", hsCode, productName);
        
        Optional<HsCodeAgencyMapping> mapping = repository.findByHsCodeAndProductCategory(hsCode, productName);
        if (mapping.isPresent()) {
            HsCodeAgencyMapping entity = mapping.get();
            entity.setUsageCount(entity.getUsageCount() + 1);
            entity.setLastUsedAt(LocalDateTime.now());
            repository.save(entity);
            log.info("사용 횟수 업데이트 완료 - 현재 사용 횟수: {}", entity.getUsageCount());
        } else {
            log.warn("매핑을 찾을 수 없음 - HS코드: {}, 상품명: {}", hsCode, productName);
        }
    }
    
    /**
     * 인기 매핑 조회 (사용 빈도 높은 순)
     */
    @Transactional(readOnly = true)
    public List<HsCodeAgencyMappingDto> getPopularMappings() {
        log.info("인기 매핑 조회");
        
        return repository.findTopUsedMappings().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 최근 사용된 매핑 조회
     */
    @Transactional(readOnly = true)
    public List<HsCodeAgencyMappingDto> getRecentlyUsedMappings() {
        log.info("최근 사용된 매핑 조회");
        
        LocalDateTime since = LocalDateTime.now().minusDays(7); // 최근 7일
        return repository.findRecentlyUsedMappings(since).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 기관을 포함하는 매핑 조회
     */
    @Transactional(readOnly = true)
    public List<HsCodeAgencyMappingDto> findByAgency(String agency) {
        log.info("기관별 매핑 조회 - 기관: {}", agency);
        
        return repository.findByAgency(agency).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 신뢰도가 높은 매핑 조회
     */
    @Transactional(readOnly = true)
    public List<HsCodeAgencyMappingDto> getHighConfidenceMappings(double minScore) {
        log.info("고신뢰도 매핑 조회 - 최소 신뢰도: {}", minScore);
        
        return repository.findByHighConfidence(minScore).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 매핑 통계 조회
     */
    @Transactional(readOnly = true)
    public HsCodeAgencyMappingStatisticsDto getStatistics() {
        log.info("매핑 통계 조회");
        
        long totalMappings = repository.count();
        List<HsCodeAgencyMapping> recentMappings = repository.findRecentlyUsedMappings(
                LocalDateTime.now().minusDays(30)
        );
        
        return HsCodeAgencyMappingStatisticsDto.builder()
                .totalMappings(totalMappings)
                .recentMappings(recentMappings.size())
                .averageConfidence(calculateAverageConfidence(recentMappings))
                .mostUsedAgency(findMostUsedAgency(recentMappings))
                .build();
    }
    
    /**
     * 엔티티를 DTO로 변환
     */
    private HsCodeAgencyMappingDto convertToDto(HsCodeAgencyMapping entity) {
        return HsCodeAgencyMappingDto.builder()
                .id(entity.getId())
                .hsCode(entity.getHsCode())
                .productCategory(entity.getProductCategory())
                .productDescription(entity.getProductDescription())
                .recommendedAgencies(Arrays.asList(entity.getRecommendedAgencies().split(",")))
                .confidenceScore(entity.getConfidenceScore())
                .usageCount(entity.getUsageCount())
                .lastUsedAt(entity.getLastUsedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    /**
     * 평균 신뢰도 계산
     */
    private Double calculateAverageConfidence(List<HsCodeAgencyMapping> mappings) {
        if (mappings.isEmpty()) {
            return 0.0;
        }
        
        return mappings.stream()
                .mapToDouble(m -> m.getConfidenceScore().doubleValue())
                .average()
                .orElse(0.0);
    }
    
    /**
     * 가장 많이 사용된 기관 찾기
     */
    private String findMostUsedAgency(List<HsCodeAgencyMapping> mappings) {
        // 간단한 구현 - 실제로는 더 복잡한 로직 필요
        return mappings.stream()
                .filter(m -> m.getUsageCount() > 0)
                .max((m1, m2) -> Integer.compare(m1.getUsageCount(), m2.getUsageCount()))
                .map(m -> m.getHsCode())
                .orElse("Unknown");
    }
}
