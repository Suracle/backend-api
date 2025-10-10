package com.suracle.backend_api.controller.requirements;

import com.suracle.backend_api.dto.requirement.HsCodeAgencyMappingDto;
import com.suracle.backend_api.dto.requirement.HsCodeAgencyMappingStatisticsDto;
import com.suracle.backend_api.dto.requirement.HsCodeAgencyMappingRequestDto;
import com.suracle.backend_api.service.requirements.HsCodeAgencyMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/hs-code-agency-mappings")
@RequiredArgsConstructor
@Slf4j
public class HsCodeAgencyMappingController {
    
    private final HsCodeAgencyMappingService service;
    
    /**
     * HS코드로 기관 매핑 조회
     */
    @GetMapping("/search")
    public ResponseEntity<HsCodeAgencyMappingDto> searchByHsCode(
            @RequestParam String hsCode,
            @RequestParam(required = false) String productName) {
        try {
            log.info("HS코드 기관 매핑 조회 - HS코드: {}, 상품명: {}", hsCode, productName);
            
            Optional<HsCodeAgencyMappingDto> result;
            if (productName != null && !productName.isEmpty()) {
                result = service.findByHsCodeAndProduct(hsCode, productName);
            } else {
                result = service.findByHsCode(hsCode);
            }
            
            return result.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
                    
        } catch (Exception e) {
            log.error("HS코드 기관 매핑 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 기관 매핑 저장
     */
    @PostMapping
    public ResponseEntity<HsCodeAgencyMappingDto> save(@RequestBody HsCodeAgencyMappingRequestDto request) {
        try {
            log.info("HS코드 기관 매핑 저장 - HS코드: {}", request.getHsCode());
            
            HsCodeAgencyMappingDto result = service.save(request);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("HS코드 기관 매핑 저장 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 사용 횟수 업데이트
     */
    @PutMapping("/usage")
    public ResponseEntity<Void> updateUsageCount(@RequestBody UsageUpdateRequest request) {
        try {
            log.info("사용 횟수 업데이트 - HS코드: {}, 상품명: {}", request.getHsCode(), request.getProductName());
            
            service.updateUsageCount(request.getHsCode(), request.getProductName());
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("사용 횟수 업데이트 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 인기 매핑 조회
     */
    @GetMapping("/popular")
    public ResponseEntity<List<HsCodeAgencyMappingDto>> getPopularMappings() {
        try {
            log.info("인기 매핑 조회");
            
            List<HsCodeAgencyMappingDto> result = service.getPopularMappings();
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("인기 매핑 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 최근 사용된 매핑 조회
     */
    @GetMapping("/recent")
    public ResponseEntity<List<HsCodeAgencyMappingDto>> getRecentlyUsedMappings() {
        try {
            log.info("최근 사용된 매핑 조회");
            
            List<HsCodeAgencyMappingDto> result = service.getRecentlyUsedMappings();
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("최근 사용된 매핑 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 기관별 매핑 조회
     */
    @GetMapping("/agency/{agency}")
    public ResponseEntity<List<HsCodeAgencyMappingDto>> findByAgency(@PathVariable String agency) {
        try {
            log.info("기관별 매핑 조회 - 기관: {}", agency);
            
            List<HsCodeAgencyMappingDto> result = service.findByAgency(agency);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("기관별 매핑 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 고신뢰도 매핑 조회
     */
    @GetMapping("/high-confidence")
    public ResponseEntity<List<HsCodeAgencyMappingDto>> getHighConfidenceMappings(
            @RequestParam(defaultValue = "0.8") double minScore) {
        try {
            log.info("고신뢰도 매핑 조회 - 최소 신뢰도: {}", minScore);
            
            List<HsCodeAgencyMappingDto> result = service.getHighConfidenceMappings(minScore);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("고신뢰도 매핑 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 매핑 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<HsCodeAgencyMappingStatisticsDto> getStatistics() {
        try {
            log.info("매핑 통계 조회");
            
            HsCodeAgencyMappingStatisticsDto result = service.getStatistics();
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("매핑 통계 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * AI를 사용하여 HS 코드 매핑 생성
     */
    @PostMapping("/generate")
    public ResponseEntity<HsCodeAgencyMappingDto> generateMappingWithAi(
            @RequestBody GenerateMappingRequest request
    ) {
        try {
            log.info("🤖 AI 매핑 생성 요청 - HS: {}, 제품: {}", request.getHsCode(), request.getProductName());
            
            HsCodeAgencyMappingDto result = service.generateAndSaveMappingWithAi(
                    request.getHsCode(),
                    request.getProductName(),
                    request.getProductCategory()
            );
            
            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
        } catch (Exception e) {
            log.error("AI 매핑 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * HS 코드 매핑 조회 또는 AI 생성 (자동)
     */
    @GetMapping("/find-or-generate")
    public ResponseEntity<HsCodeAgencyMappingDto> findOrGenerateMapping(
            @RequestParam String hsCode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productCategory
    ) {
        try {
            log.info("🔍 매핑 조회 또는 생성 - HS: {}", hsCode);
            
            HsCodeAgencyMappingDto result = service.findOrGenerateMapping(
                    hsCode,
                    productName != null ? productName : "",
                    productCategory != null ? productCategory : ""
            );
            
            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("매핑 조회/생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * AI 매핑 생성 요청 DTO
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    static class GenerateMappingRequest {
        private String hsCode;
        private String productName;
        private String productCategory;
    }
    
    /**
     * 사용 횟수 업데이트 요청 DTO
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    static class UsageUpdateRequest {
        private String hsCode;
        private String productName;
    }
}
