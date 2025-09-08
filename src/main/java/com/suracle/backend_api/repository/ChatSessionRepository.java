package com.suracle.backend_api.repository;

import com.suracle.backend_api.entity.chat.ChatSession;
import com.suracle.backend_api.entity.chat.enums.ChatSessionStatus;
import com.suracle.backend_api.entity.chat.enums.ChatSessionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Integer> {
    
    /**
     * 사용자 ID로 활성 세션 조회
     * @param userId 사용자 ID
     * @param status 세션 상태
     * @return 활성 세션 목록
     */
    List<ChatSession> findByUserIdAndStatusOrderByCreatedAtDesc(Integer userId, ChatSessionStatus status);
    
    /**
     * 사용자 ID와 세션 타입으로 활성 세션 조회
     * @param userId 사용자 ID
     * @param sessionType 세션 타입
     * @param status 세션 상태
     * @return 활성 세션 목록
     */
    List<ChatSession> findByUserIdAndSessionTypeAndStatusOrderByCreatedAtDesc(
            Integer userId, ChatSessionType sessionType, ChatSessionStatus status);
    
    /**
     * 만료된 세션 조회 (일정 시간 이전에 생성된 세션)
     * @param expiredTime 만료 시간
     * @return 만료된 세션 목록
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.createdAt < :expiredTime")
    List<ChatSession> findExpiredSessions(@Param("expiredTime") LocalDateTime expiredTime);
    
    /**
     * 사용자 ID로 최근 세션 조회
     * @param userId 사용자 ID
     * @return 최근 세션
     */
    Optional<ChatSession> findFirstByUserIdOrderByCreatedAtDesc(Integer userId);
}
