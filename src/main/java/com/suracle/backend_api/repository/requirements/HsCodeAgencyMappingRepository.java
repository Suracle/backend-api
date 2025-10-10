package com.suracle.backend_api.repository.requirements;

import com.suracle.backend_api.entity.requirements.HsCodeAgencyMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HsCodeAgencyMappingRepository extends JpaRepository<HsCodeAgencyMapping, Long> {
    
    /**
     * HS코드로 기관 매핑 조회
     */
    Optional<HsCodeAgencyMapping> findByHsCode(String hsCode);
    
    /**
     * HS코드와 상품 카테고리로 기관 매핑 조회
     */
    Optional<HsCodeAgencyMapping> findByHsCodeAndProductCategory(String hsCode, String productCategory);
    
    /**
     * 사용 빈도가 높은 매핑 조회 (캐시용)
     */
    @Query("SELECT h FROM HsCodeAgencyMapping h WHERE h.usageCount > 0 ORDER BY h.usageCount DESC, h.lastUsedAt DESC")
    List<HsCodeAgencyMapping> findTopUsedMappings();
    
    /**
     * 최근 사용된 매핑 조회
     */
    @Query("SELECT h FROM HsCodeAgencyMapping h WHERE h.lastUsedAt >= :since ORDER BY h.lastUsedAt DESC")
    List<HsCodeAgencyMapping> findRecentlyUsedMappings(@Param("since") LocalDateTime since);
    
    /**
     * 특정 기관을 포함하는 매핑 조회
     * JSONB 필드에서 검색하기 위해 네이티브 쿼리 사용
     */
    @Query(value = "SELECT * FROM hs_code_agency_mappings WHERE recommended_agencies::text LIKE CONCAT('%', :agency, '%')", nativeQuery = true)
    List<HsCodeAgencyMapping> findByAgency(@Param("agency") String agency);
    
    /**
     * 신뢰도가 높은 매핑 조회
     */
    @Query("SELECT h FROM HsCodeAgencyMapping h WHERE h.confidenceScore >= :minScore ORDER BY h.confidenceScore DESC")
    List<HsCodeAgencyMapping> findByHighConfidence(@Param("minScore") Double minScore);
}
