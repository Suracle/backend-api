package com.suracle.backend_api.repository.requirements;

import com.suracle.backend_api.entity.requirements.AgencySearchStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgencySearchStrategyRepository extends JpaRepository<AgencySearchStrategy, Long> {
    
    /**
     * 기관명으로 검색 전략 조회
     */
    Optional<AgencySearchStrategy> findByAgencyName(String agencyName);
    
    /**
     * 활성화된 검색 전략 조회
     */
    List<AgencySearchStrategy> findByIsActiveTrue();
    
    /**
     * 검색 제공자별 전략 조회
     */
    List<AgencySearchStrategy> findBySearchProvider(String searchProvider);
    
    /**
     * 무료 API 전략 조회
     */
    @Query("SELECT a FROM AgencySearchStrategy a WHERE a.searchProvider = 'free_api' AND a.isActive = true")
    List<AgencySearchStrategy> findFreeApiStrategies();
    
    /**
     * 유료 API 전략 조회
     */
    @Query("SELECT a FROM AgencySearchStrategy a WHERE a.searchProvider != 'free_api' AND a.isActive = true")
    List<AgencySearchStrategy> findPaidApiStrategies();
    
    /**
     * 비용이 있는 전략 조회
     */
    @Query("SELECT a FROM AgencySearchStrategy a WHERE a.costPerRequest > 0 AND a.isActive = true")
    List<AgencySearchStrategy> findCostlyStrategies();
}
