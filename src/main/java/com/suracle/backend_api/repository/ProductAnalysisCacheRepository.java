package com.suracle.backend_api.repository;

import com.suracle.backend_api.entity.cache.ProductAnalysisCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductAnalysisCacheRepository extends JpaRepository<ProductAnalysisCache, Integer> {
    Optional<ProductAnalysisCache> findByProductIdAndAnalysisType(Integer productId, String analysisType);
}
