package com.lawgenie.backend_api.service;

import com.lawgenie.backend_api.dto.user.LoginRequestDto;
import com.lawgenie.backend_api.dto.user.LoginResponseDto;
import com.lawgenie.backend_api.dto.user.SignupRequestDto;
import com.lawgenie.backend_api.dto.user.SignupResponseDto;

public interface UserService {
    
    /**
     * 회원가입
     * @param signupRequestDto 회원가입 요청 정보
     * @return 회원가입된 사용자 정보
     */
    SignupResponseDto signup(SignupRequestDto signupRequestDto);
    
    /**
     * 로그인
     * @param loginRequestDto 로그인 요청 정보
     * @return 로그인된 사용자 정보
     */
    LoginResponseDto login(LoginRequestDto loginRequestDto);
    
    /**
     * 이메일 중복 확인
     * @param email 이메일
     * @return 중복 여부
     */
    boolean isEmailDuplicate(String email);
}
