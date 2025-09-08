package com.suracle.backend_api.dto.chat;

import com.suracle.backend_api.entity.chat.enums.ChatSessionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionRequestDto {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    private Integer userId;
    
    @NotNull(message = "세션 타입은 필수입니다")
    private ChatSessionType sessionType;
    
    @NotNull(message = "언어는 필수입니다")
    private String language;
    
    private String sessionData;
}
