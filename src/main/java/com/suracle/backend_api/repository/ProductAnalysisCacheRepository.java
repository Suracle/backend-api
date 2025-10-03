package com.suracle.backend_api.repository;

import com.suracle.backend_api.entity.cache.ProductAnalysisCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProductAnalysisCacheRepository extends JpaRepository<ProductAnalysisCache, Integer> {
    Optional<ProductAnalysisCache> findByProductIdAndAnalysisType(Integer productId, String analysisType);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ProductAnalysisCache p WHERE p.product.id = :productId AND p.analysisType = :analysisType")
    void deleteByProductIdAndAnalysisType(@Param("productId") Integer productId, @Param("analysisType") String analysisType);
    
    /**
     * HS코드와 분석 타입으로 캐시 검색 (AI 엔진용)
     */
    @Query("SELECT p FROM ProductAnalysisCache p WHERE p.product.hsCode = :hsCode AND p.analysisType = :analysisType")
    List<ProductAnalysisCache> findByProductHsCodeAndAnalysisType(@Param("hsCode") String hsCode, @Param("analysisType") String analysisType);
}
