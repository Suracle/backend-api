package com.suracle.backend_api.repository;

import com.suracle.backend_api.entity.chat.ChatMessage;
import com.suracle.backend_api.entity.chat.enums.MessageSenderType;
import com.suracle.backend_api.entity.chat.enums.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    
    /**
     * 세션 ID로 메시지 목록 조회 (최신순)
     * @param sessionId 세션 ID
     * @param pageable 페이징 정보
     * @return 메시지 목록
     */
    Page<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Integer sessionId, Pageable pageable);
    
    /**
     * 세션 ID로 메시지 목록 조회 (페이징 없이)
     * @param sessionId 세션 ID
     * @return 메시지 목록
     */
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Integer sessionId);
    
    /**
     * 세션 ID와 발신자 타입으로 메시지 조회
     * @param sessionId 세션 ID
     * @param senderType 발신자 타입
     * @return 메시지 목록
     */
    List<ChatMessage> findBySessionIdAndSenderTypeOrderByCreatedAtAsc(Integer sessionId, MessageSenderType senderType);
    
    /**
     * 세션 ID와 메시지 타입으로 메시지 조회
     * @param sessionId 세션 ID
     * @param messageType 메시지 타입
     * @return 메시지 목록
     */
    List<ChatMessage> findBySessionIdAndMessageTypeOrderByCreatedAtAsc(Integer sessionId, MessageType messageType);
    
    /**
     * 만료된 메시지 조회 (일정 시간 이전에 생성된 메시지)
     * @param expiredTime 만료 시간
     * @return 만료된 메시지 목록
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.createdAt < :expiredTime")
    List<ChatMessage> findExpiredMessages(@Param("expiredTime") LocalDateTime expiredTime);
    
    /**
     * 세션의 마지막 메시지 조회
     * @param sessionId 세션 ID
     * @return 마지막 메시지
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.session.id = :sessionId ORDER BY cm.createdAt DESC LIMIT 1")
    ChatMessage findLastMessageBySessionId(@Param("sessionId") Integer sessionId);
}
