package com.suracle.backend_api.repository;

import com.suracle.backend_api.entity.hs.HsCodeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HsCodeAnalysisRepository extends JpaRepository<HsCodeAnalysis, Integer> {
    
    /**
     * 분석 세션 ID로 HS코드 분석 결과 조회 (정확도 순으로 정렬)
     */
    @Query("SELECT h FROM HsCodeAnalysis h WHERE h.analysisSessionId = :sessionId ORDER BY h.confidenceScore DESC")
    List<HsCodeAnalysis> findByAnalysisSessionIdOrderByConfidenceScoreDesc(@Param("sessionId") String sessionId);
    
    /**
     * 분석 세션 ID로 선택된 HS코드 분석 결과 조회
     */
    @Query("SELECT h FROM HsCodeAnalysis h WHERE h.analysisSessionId = :sessionId AND h.isSelected = true")
    Optional<HsCodeAnalysis> findSelectedByAnalysisSessionId(@Param("sessionId") String sessionId);
    
    /**
     * 분석 ID로 HS코드 분석 결과 조회
     */
    Optional<HsCodeAnalysis> findByIdAndIsSelectedTrue(Integer id);
}
