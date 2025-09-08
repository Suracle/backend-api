package com.suracle.backend_api.dto.chat;

import com.suracle.backend_api.entity.chat.enums.MessageSenderType;
import com.suracle.backend_api.entity.chat.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponseDto {
    
    private Integer id;
    private Integer sessionId;
    private MessageSenderType senderType;
    private String messageContent;
    private MessageType messageType;
    private String metadata;
    private LocalDateTime createdAt;
}
