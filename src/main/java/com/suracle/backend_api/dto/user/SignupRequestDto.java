package com.suracle.backend_api.dto.user;

import com.suracle.backend_api.entity.user.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {

    private String email;
    private String password;
    private UserType userType;
    private String userName;
}
