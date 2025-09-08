package com.suracle.backend_api.dto.chat;

import com.suracle.backend_api.entity.chat.enums.MessageSenderType;
import com.suracle.backend_api.entity.chat.enums.MessageType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequestDto {
    
    @NotNull(message = "세션 ID는 필수입니다")
    private Integer sessionId;
    
    @NotNull(message = "발신자 타입은 필수입니다")
    private MessageSenderType senderType;
    
    @NotNull(message = "메시지 내용은 필수입니다")
    private String messageContent;
    
    @NotNull(message = "메시지 타입은 필수입니다")
    private MessageType messageType;
    
    private String metadata;
}
