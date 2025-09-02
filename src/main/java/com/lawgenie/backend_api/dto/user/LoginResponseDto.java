package com.lawgenie.backend_api.dto.user;

import com.lawgenie.backend_api.entity.user.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private Integer id;
    private String email;
    private UserType userType;
    private String userName;
    private String preferredLanguage;
    private Boolean isActive;
    private String message;
}
