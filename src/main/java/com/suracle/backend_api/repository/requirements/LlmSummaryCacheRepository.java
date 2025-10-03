package com.suracle.backend_api.repository.requirements;

import com.suracle.backend_api.entity.requirements.LlmSummaryCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LlmSummaryCacheRepository extends JpaRepository<LlmSummaryCache, Long> {
    
    /**
     * HS코드, 상품명, 문서 해시로 요약 결과 조회
     */
    @Query("SELECT l FROM LlmSummaryCache l WHERE l.hsCode = :hsCode AND l.productName = :productName AND l.rawDocumentsHash = :hash AND l.expiresAt > :now")
    Optional<LlmSummaryCache> findValidByHsCodeAndProductAndHash(@Param("hsCode") String hsCode, 
                                                                 @Param("productName") String productName, 
                                                                 @Param("hash") String hash, 
                                                                 @Param("now") LocalDateTime now);
    
    /**
     * 만료된 LLM 캐시 삭제
     */
    @Modifying
    @Query("DELETE FROM LlmSummaryCache l WHERE l.expiresAt < :now")
    int deleteExpiredCache(@Param("now") LocalDateTime now);
    
    /**
     * 모델별 사용 통계 조회
     */
    @Query("SELECT l.modelUsed, COUNT(l), SUM(l.tokensUsed), SUM(l.cost) FROM LlmSummaryCache l WHERE l.expiresAt > :now GROUP BY l.modelUsed")
    List<Object[]> getModelUsageStats(@Param("now") LocalDateTime now);
    
    /**
     * 비용 통계 조회
     */
    @Query("SELECT SUM(l.cost), AVG(l.cost), MAX(l.cost) FROM LlmSummaryCache l WHERE l.expiresAt > :now")
    Object[] getCostStats(@Param("now") LocalDateTime now);
}
