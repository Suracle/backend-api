package com.suracle.backend_api.service.impl;

import com.suracle.backend_api.dto.chat.ChatMessageRequestDto;
import com.suracle.backend_api.dto.chat.ChatMessageResponseDto;
import com.suracle.backend_api.dto.chat.ChatSessionRequestDto;
import com.suracle.backend_api.dto.chat.ChatSessionResponseDto;
import com.suracle.backend_api.entity.chat.ChatMessage;
import com.suracle.backend_api.entity.chat.ChatSession;
import com.suracle.backend_api.entity.chat.enums.ChatSessionStatus;
import com.suracle.backend_api.entity.chat.enums.ChatSessionType;
import com.suracle.backend_api.entity.chat.enums.MessageSenderType;
import com.suracle.backend_api.entity.chat.enums.MessageType;
import com.suracle.backend_api.entity.user.User;
import com.suracle.backend_api.repository.ChatMessageRepository;
import com.suracle.backend_api.repository.ChatSessionRepository;
import com.suracle.backend_api.repository.UserRepository;
import com.suracle.backend_api.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatAiService chatAiService;

    @Override
    public ChatSessionResponseDto createSession(ChatSessionRequestDto requestDto) {
        log.info("새 채팅 세션 생성 요청 - 사용자 ID: {}, 세션 타입: {}", requestDto.getUserId(), requestDto.getSessionType());
        
        // 사용자 존재 확인
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + requestDto.getUserId()));
        
        // 기존 활성 세션이 있는지 확인 (같은 타입의 세션)
        List<ChatSession> existingSessions = chatSessionRepository.findByUserIdAndSessionTypeAndStatusOrderByCreatedAtDesc(
                requestDto.getUserId(), requestDto.getSessionType(), ChatSessionStatus.ACTIVE);
        
        if (!existingSessions.isEmpty()) {
            log.info("기존 활성 세션 발견 - 세션 ID: {}", existingSessions.get(0).getId());
            return convertToSessionResponseDto(existingSessions.get(0));
        }
        
        // 새 세션 생성
        ChatSession session = ChatSession.builder()
                .user(user)
                .sessionType(requestDto.getSessionType())
                .language(requestDto.getLanguage())
                .status(ChatSessionStatus.ACTIVE)
                .sessionData(requestDto.getSessionData())
                .build();
        
        ChatSession savedSession = chatSessionRepository.save(session);
        log.info("새 채팅 세션 생성 완료 - 세션 ID: {}", savedSession.getId());
        
        return convertToSessionResponseDto(savedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatSessionResponseDto getSession(Integer sessionId) {
        log.info("세션 조회 요청 - 세션 ID: {}", sessionId);
        
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다: " + sessionId));
        
        return convertToSessionResponseDto(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatSessionResponseDto> getActiveSessions(Integer userId, ChatSessionType sessionType) {
        log.info("활성 세션 조회 요청 - 사용자 ID: {}, 세션 타입: {}", userId, sessionType);
        
        List<ChatSession> sessions;
        if (sessionType != null) {
            sessions = chatSessionRepository.findByUserIdAndSessionTypeAndStatusOrderByCreatedAtDesc(
                    userId, sessionType, ChatSessionStatus.ACTIVE);
        } else {
            sessions = chatSessionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                    userId, ChatSessionStatus.ACTIVE);
        }
        
        return sessions.stream()
                .map(this::convertToSessionResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ChatSessionResponseDto updateSession(Integer sessionId, ChatSessionStatus status, String sessionData) {
        log.info("세션 업데이트 요청 - 세션 ID: {}, 상태: {}", sessionId, status);
        
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다: " + sessionId));
        
        session.setStatus(status);
        if (sessionData != null) {
            session.setSessionData(sessionData);
        }
        
        ChatSession updatedSession = chatSessionRepository.save(session);
        log.info("세션 업데이트 완료 - 세션 ID: {}", updatedSession.getId());
        
        return convertToSessionResponseDto(updatedSession);
    }

    @Override
    public ChatMessageResponseDto sendMessage(ChatMessageRequestDto requestDto) {
        log.info("메시지 전송 요청 - 세션 ID: {}, 발신자: {}", requestDto.getSessionId(), requestDto.getSenderType());
        
        // 세션 존재 확인
        ChatSession session = chatSessionRepository.findById(requestDto.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다: " + requestDto.getSessionId()));
        
        // 메시지 생성
        ChatMessage message = ChatMessage.builder()
                .session(session)
                .senderType(requestDto.getSenderType())
                .messageContent(requestDto.getMessageContent())
                .messageType(requestDto.getMessageType())
                .metadata(requestDto.getMetadata())
                .build();
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        log.info("메시지 전송 완료 - 메시지 ID: {}", savedMessage.getId());
        
        return convertToMessageResponseDto(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponseDto> getMessages(Integer sessionId, Pageable pageable) {
        log.info("메시지 목록 조회 요청 - 세션 ID: {}, 페이지: {}", sessionId, pageable.getPageNumber());
        
        // 세션 존재 확인
        chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다: " + sessionId));
        
        Page<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId, pageable);
        
        return messages.map(this::convertToMessageResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDto> getAllMessages(Integer sessionId) {
        log.info("모든 메시지 조회 요청 - 세션 ID: {}", sessionId);
        
        // 세션 존재 확인
        chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다: " + sessionId));
        
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        
        return messages.stream()
                .map(this::convertToMessageResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public int cleanupExpiredData(int expiredHours) {
        log.info("만료된 데이터 정리 시작 - 만료 시간: {}시간", expiredHours);
        
        LocalDateTime expiredTime = LocalDateTime.now().minusHours(expiredHours);
        
        // 만료된 메시지 삭제
        List<ChatMessage> expiredMessages = chatMessageRepository.findExpiredMessages(expiredTime);
        chatMessageRepository.deleteAll(expiredMessages);
        
        // 만료된 세션 삭제
        List<ChatSession> expiredSessions = chatSessionRepository.findExpiredSessions(expiredTime);
        chatSessionRepository.deleteAll(expiredSessions);
        
        int totalCleaned = expiredMessages.size() + expiredSessions.size();
        log.info("만료된 데이터 정리 완료 - 삭제된 항목 수: {}", totalCleaned);
        
        return totalCleaned;
    }

    @Override
    public ChatMessageResponseDto generateAiResponse(Integer sessionId, String userMessage) {
        log.info("AI 응답 생성 요청 - 세션 ID: {}, 사용자 메시지: {}", sessionId, userMessage);
        
        // 세션 조회
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다: " + sessionId));
        
        // 샘플 데이터 기반 AI 응답 생성 (추후 실제 AI 로직으로 대체)
        String aiResponse = generateSampleAiResponse(session, userMessage);
        
        // AI 응답 메시지 저장
        ChatMessage aiMessage = ChatMessage.builder()
                .session(session)
                .senderType(MessageSenderType.AI)
                .messageContent(aiResponse)
                .messageType(MessageType.TEXT)
                .metadata("{\"ai_generated\": true, \"response_time_ms\": 1500}")
                .build();
        
        ChatMessage savedMessage = chatMessageRepository.save(aiMessage);
        log.info("AI 응답 생성 완료 - 메시지 ID: {}", savedMessage.getId());
        
        return convertToMessageResponseDto(savedMessage);
    }

    /**
     * 샘플 데이터 기반 AI 응답 생성 (MVP용)
     * 추후 실제 AI 로직으로 대체 예정
     */
    private String generateSampleAiResponse(ChatSession session, String userMessage) {
        return chatAiService.generateResponse(session, userMessage);
    }

    /**
     * ChatSession을 ChatSessionResponseDto로 변환
     */
    private ChatSessionResponseDto convertToSessionResponseDto(ChatSession session) {
        return ChatSessionResponseDto.builder()
                .id(session.getId())
                .userId(session.getUser().getId())
                .userName(session.getUser().getUserName())
                .sessionType(session.getSessionType())
                .language(session.getLanguage())
                .status(session.getStatus())
                .sessionData(session.getSessionData())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    /**
     * ChatMessage를 ChatMessageResponseDto로 변환
     */
    private ChatMessageResponseDto convertToMessageResponseDto(ChatMessage message) {
        return ChatMessageResponseDto.builder()
                .id(message.getId())
                .sessionId(message.getSession().getId())
                .senderType(message.getSenderType())
                .messageContent(message.getMessageContent())
                .messageType(message.getMessageType())
                .metadata(message.getMetadata())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
