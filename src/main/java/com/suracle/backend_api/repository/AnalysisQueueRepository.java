package com.suracle.backend_api.repository;

import com.suracle.backend_api.entity.cache.AnalysisQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalysisQueueRepository extends JpaRepository<AnalysisQueue, Integer> {
    
    /**
     * 대기 중인 분석 큐 항목들을 우선순위와 스케줄 시간 순으로 조회
     */
    @Query("SELECT a FROM AnalysisQueue a WHERE a.status = 'PENDING' AND a.scheduledAt <= :now ORDER BY a.priority DESC, a.scheduledAt ASC")
    List<AnalysisQueue> findPendingAnalyses(@Param("now") LocalDateTime now);
    
    /**
     * 상품 ID로 분석 큐 항목 조회
     */
    List<AnalysisQueue> findByProductIdOrderByCreatedAtDesc(Integer productId);
    
    /**
     * 상품 ID와 상태로 분석 큐 항목 조회
     */
    List<AnalysisQueue> findByProductIdAndStatusOrderByCreatedAtDesc(Integer productId, AnalysisQueue.QueueStatus status);
}
