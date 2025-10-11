package com.suracle.backend_api.controller;

import com.suracle.backend_api.dto.chat.ChatMessageRequestDto;
import com.suracle.backend_api.dto.chat.ChatMessageResponseDto;
import com.suracle.backend_api.dto.chat.ChatSessionRequestDto;
import com.suracle.backend_api.dto.chat.ChatSessionResponseDto;
import com.suracle.backend_api.entity.chat.enums.ChatSessionStatus;
import com.suracle.backend_api.entity.chat.enums.ChatSessionType;
import com.suracle.backend_api.service.ChatService2;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService2 chatService;

    /**
     * 새 채팅 세션 생성
     * 
     * @param requestDto 세션 생성 요청 정보
     * @return 생성된 세션 정보
     */
    @PostMapping("/sessions")
    public ResponseEntity<ChatSessionResponseDto> createSession(@Valid @RequestBody ChatSessionRequestDto requestDto) {
        try {
            log.info("새 채팅 세션 생성 요청 - 사용자 ID: {}, 세션 타입: {}", requestDto.getUserId(), requestDto.getSessionType());
            ChatSessionResponseDto response = chatService.createSession(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("세션 생성 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("세션 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 세션 조회
     * 
     * @param sessionId 세션 ID
     * @return 세션 정보
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatSessionResponseDto> getSession(@PathVariable Integer sessionId) {
        try {
            log.info("세션 조회 요청 - 세션 ID: {}", sessionId);
            ChatSessionResponseDto response = chatService.getSession(sessionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("세션 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("세션 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 사용자의 활성 세션 조회
     * 
     * @param userId      사용자 ID
     * @param sessionType 세션 타입 (선택사항)
     * @return 활성 세션 목록
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionResponseDto>> getActiveSessions(
            @RequestParam Integer userId,
            @RequestParam(required = false) ChatSessionType sessionType) {
        try {
            log.info("활성 세션 조회 요청 - 사용자 ID: {}, 세션 타입: {}", userId, sessionType);
            List<ChatSessionResponseDto> responses = chatService.getActiveSessions(userId, sessionType);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("활성 세션 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 세션 상태 업데이트
     * 
     * @param sessionId   세션 ID
     * @param status      새로운 상태
     * @param sessionData 세션 데이터 (선택사항)
     * @return 업데이트된 세션 정보
     */
    @PutMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatSessionResponseDto> updateSession(
            @PathVariable Integer sessionId,
            @RequestParam ChatSessionStatus status,
            @RequestParam(required = false) String sessionData) {
        try {
            log.info("세션 업데이트 요청 - 세션 ID: {}, 상태: {}", sessionId, status);
            ChatSessionResponseDto response = chatService.updateSession(sessionId, status, sessionData);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("세션 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("세션 업데이트 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 메시지 전송
     * 
     * @param sessionId  세션 ID
     * @param requestDto 메시지 전송 요청 정보
     * @return 전송된 메시지 정보
     */
    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ChatMessageResponseDto> sendMessage(
            @PathVariable Integer sessionId,
            @Valid @RequestBody ChatMessageRequestDto requestDto) {
        try {
            log.info("메시지 전송 요청 - 세션 ID: {}, 발신자: {}", sessionId, requestDto.getSenderType());

            // 세션 ID 설정
            requestDto.setSessionId(sessionId);

            ChatMessageResponseDto response = chatService.sendMessage(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("메시지 전송 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("메시지 전송 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 세션의 메시지 목록 조회 (페이징)
     * 
     * @param sessionId 세션 ID
     * @param page      페이지 번호 (기본값: 0)
     * @param size      페이지 크기 (기본값: 20)
     * @param sort      정렬 기준 (기본값: createdAt,asc)
     * @return 메시지 목록
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<Page<ChatMessageResponseDto>> getMessages(
            @PathVariable Integer sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,asc") String sort) {
        try {
            log.info("메시지 목록 조회 요청 - 세션 ID: {}, 페이지: {}, 크기: {}", sessionId, page, size);

            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

            Page<ChatMessageResponseDto> responses = chatService.getMessages(sessionId, pageable);
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            log.warn("메시지 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("메시지 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 세션의 모든 메시지 조회 (페이징 없이)
     * 
     * @param sessionId 세션 ID
     * @return 메시지 목록
     */
    @GetMapping("/sessions/{sessionId}/messages/all")
    public ResponseEntity<List<ChatMessageResponseDto>> getAllMessages(@PathVariable Integer sessionId) {
        try {
            log.info("모든 메시지 조회 요청 - 세션 ID: {}", sessionId);
            List<ChatMessageResponseDto> responses = chatService.getAllMessages(sessionId);
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            log.warn("메시지 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("메시지 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * AI 응답 생성
     * 
     * @param sessionId   세션 ID
     * @param userMessage 사용자 메시지
     * @return AI 응답 메시지
     */
    @PostMapping("/sessions/{sessionId}/ai-response")
    public ResponseEntity<ChatMessageResponseDto> generateAiResponse(
            @PathVariable Integer sessionId,
            @RequestParam String userMessage) {
        try {
            log.info("AI 응답 생성 요청 - 세션 ID: {}, 사용자 메시지: {}", sessionId, userMessage);
            ChatMessageResponseDto response = chatService.generateAiResponse(sessionId, userMessage);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("AI 응답 생성 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("AI 응답 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 만료된 데이터 정리 (관리자용)
     * 
     * @param expiredHours 만료 시간 (시간 단위, 기본값: 24)
     * @return 정리된 항목 수
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Integer> cleanupExpiredData(@RequestParam(defaultValue = "24") int expiredHours) {
        try {
            log.info("만료된 데이터 정리 요청 - 만료 시간: {}시간", expiredHours);
            int cleanedCount = chatService.cleanupExpiredData(expiredHours);
            return ResponseEntity.ok(cleanedCount);
        } catch (Exception e) {
            log.error("데이터 정리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
