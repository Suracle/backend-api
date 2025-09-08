package com.suracle.backend_api.service;

import com.suracle.backend_api.dto.chat.ChatMessageRequestDto;
import com.suracle.backend_api.dto.chat.ChatMessageResponseDto;
import com.suracle.backend_api.dto.chat.ChatSessionRequestDto;
import com.suracle.backend_api.dto.chat.ChatSessionResponseDto;
import com.suracle.backend_api.entity.chat.enums.ChatSessionStatus;
import com.suracle.backend_api.entity.chat.enums.ChatSessionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {
    
    /**
     * 새 채팅 세션 생성
     * @param requestDto 세션 생성 요청 정보
     * @return 생성된 세션 정보
     */
    ChatSessionResponseDto createSession(ChatSessionRequestDto requestDto);
    
    /**
     * 세션 조회
     * @param sessionId 세션 ID
     * @return 세션 정보
     */
    ChatSessionResponseDto getSession(Integer sessionId);
    
    /**
     * 사용자의 활성 세션 조회
     * @param userId 사용자 ID
     * @param sessionType 세션 타입 (선택사항)
     * @return 활성 세션 목록
     */
    List<ChatSessionResponseDto> getActiveSessions(Integer userId, ChatSessionType sessionType);
    
    /**
     * 세션 상태 업데이트
     * @param sessionId 세션 ID
     * @param status 새로운 상태
     * @param sessionData 세션 데이터 (선택사항)
     * @return 업데이트된 세션 정보
     */
    ChatSessionResponseDto updateSession(Integer sessionId, ChatSessionStatus status, String sessionData);
    
    /**
     * 메시지 전송
     * @param requestDto 메시지 전송 요청 정보
     * @return 전송된 메시지 정보
     */
    ChatMessageResponseDto sendMessage(ChatMessageRequestDto requestDto);
    
    /**
     * 세션의 메시지 목록 조회
     * @param sessionId 세션 ID
     * @param pageable 페이징 정보
     * @return 메시지 목록
     */
    Page<ChatMessageResponseDto> getMessages(Integer sessionId, Pageable pageable);
    
    /**
     * 세션의 모든 메시지 조회 (페이징 없이)
     * @param sessionId 세션 ID
     * @return 메시지 목록
     */
    List<ChatMessageResponseDto> getAllMessages(Integer sessionId);
    
    /**
     * 만료된 세션 및 메시지 정리
     * @param expiredHours 만료 시간 (시간 단위)
     * @return 정리된 세션 수
     */
    int cleanupExpiredData(int expiredHours);
    
    /**
     * 샘플 데이터 기반 AI 응답 생성
     * @param sessionId 세션 ID
     * @param userMessage 사용자 메시지
     * @return AI 응답 메시지
     */
    ChatMessageResponseDto generateAiResponse(Integer sessionId, String userMessage);
}
