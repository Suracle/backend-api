package com.suracle.backend_api.dto.chat;

import com.suracle.backend_api.entity.chat.enums.ChatSessionStatus;
import com.suracle.backend_api.entity.chat.enums.ChatSessionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionResponseDto {
    
    private Integer id;
    private Integer userId;
    private String userName;
    private ChatSessionType sessionType;
    private String language;
    private ChatSessionStatus status;
    private String sessionData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
