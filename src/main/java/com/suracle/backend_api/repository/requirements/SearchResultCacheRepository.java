package com.suracle.backend_api.repository.requirements;

import com.suracle.backend_api.entity.requirements.SearchResultCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SearchResultCacheRepository extends JpaRepository<SearchResultCache, Long> {
    
    /**
     * 캐시 키로 검색 결과 조회
     */
    Optional<SearchResultCache> findByCacheKey(String cacheKey);
    
    /**
     * HS코드와 기관으로 검색 결과 조회
     */
    @Query("SELECT s FROM SearchResultCache s WHERE s.hsCode = :hsCode AND s.agency = :agency AND s.expiresAt > :now")
    List<SearchResultCache> findValidByHsCodeAndAgency(@Param("hsCode") String hsCode, 
                                                       @Param("agency") String agency, 
                                                       @Param("now") LocalDateTime now);
    
    /**
     * 만료된 캐시 삭제
     */
    @Modifying
    @Query("DELETE FROM SearchResultCache s WHERE s.expiresAt < :now")
    int deleteExpiredCache(@Param("now") LocalDateTime now);
    
    /**
     * 특정 HS코드의 모든 캐시 조회
     */
    @Query("SELECT s FROM SearchResultCache s WHERE s.hsCode = :hsCode AND s.expiresAt > :now")
    List<SearchResultCache> findValidByHsCode(@Param("hsCode") String hsCode, @Param("now") LocalDateTime now);
    
    /**
     * 캐시 사용 통계 조회
     */
    @Query("SELECT s.agency, COUNT(s) FROM SearchResultCache s WHERE s.expiresAt > :now GROUP BY s.agency")
    List<Object[]> getCacheUsageStats(@Param("now") LocalDateTime now);
}
