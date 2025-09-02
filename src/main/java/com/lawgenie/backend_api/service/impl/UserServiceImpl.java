package com.lawgenie.backend_api.service.impl;

import com.lawgenie.backend_api.dto.user.LoginRequestDto;
import com.lawgenie.backend_api.dto.user.LoginResponseDto;
import com.lawgenie.backend_api.dto.user.SignupRequestDto;
import com.lawgenie.backend_api.dto.user.SignupResponseDto;
import com.lawgenie.backend_api.entity.user.User;
import com.lawgenie.backend_api.entity.user.enums.UserType;
import com.lawgenie.backend_api.repository.UserRepository;
import com.lawgenie.backend_api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {
        log.info("회원가입 요청: {}", signupRequestDto.getEmail());
        
        // 기본 유효성 검증
        validateSignupRequest(signupRequestDto);
        
        // 이메일 중복 확인
        if (isEmailDuplicate(signupRequestDto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + signupRequestDto.getEmail());
        }
        
        // 사용자 유형별 기본 언어 설정
        String preferredLanguage = getDefaultLanguage(signupRequestDto.getUserType());
        
        // User 엔티티 생성
        User user = User.builder()
                .email(signupRequestDto.getEmail())
                .password(signupRequestDto.getPassword()) // 실제로는 암호화 필요
                .userType(signupRequestDto.getUserType())
                .userName(signupRequestDto.getUserName())
                .preferredLanguage(preferredLanguage)
                .isActive(true)
                .build();
        
        // 사용자 저장
        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: {}", savedUser.getEmail());
        
        // 응답 DTO 생성
        return SignupResponseDto.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .userType(savedUser.getUserType())
                .userName(savedUser.getUserName())
                .preferredLanguage(savedUser.getPreferredLanguage())
                .isActive(savedUser.getIsActive())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        log.info("로그인 요청: {}", loginRequestDto.getEmail());
        
        // 기본 유효성 검증
        validateLoginRequest(loginRequestDto);
        
        // 사용자 조회
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다: " + loginRequestDto.getEmail()));
        
        // 비밀번호 확인 (실제로는 암호화된 비밀번호와 비교해야 함)
        if (!user.getPassword().equals(loginRequestDto.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
        
        // 비활성 사용자 확인
        if (!user.getIsActive()) {
            throw new IllegalArgumentException("비활성화된 계정입니다");
        }
        
        log.info("로그인 성공: {}", user.getEmail());
        
        // 응답 DTO 생성
        return LoginResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .userType(user.getUserType())
                .userName(user.getUserName())
                .preferredLanguage(user.getPreferredLanguage())
                .isActive(user.getIsActive())
                .message("로그인 성공")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * 회원가입 요청 유효성 검증
     * @param signupRequestDto 회원가입 요청 정보
     */
    private void validateSignupRequest(SignupRequestDto signupRequestDto) {
        if (signupRequestDto.getEmail() == null || signupRequestDto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (signupRequestDto.getPassword() == null || signupRequestDto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("패스워드는 필수입니다");
        }
        if (signupRequestDto.getUserType() == null) {
            throw new IllegalArgumentException("사용자 유형은 필수입니다");
        }
        if (signupRequestDto.getUserName() == null || signupRequestDto.getUserName().trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수입니다");
        }
    }
    
    /**
     * 로그인 요청 유효성 검증
     * @param loginRequestDto 로그인 요청 정보
     */
    private void validateLoginRequest(LoginRequestDto loginRequestDto) {
        if (loginRequestDto.getEmail() == null || loginRequestDto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (loginRequestDto.getPassword() == null || loginRequestDto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("패스워드는 필수입니다");
        }
    }
    
    /**
     * 사용자 유형별 기본 언어 설정
     * @param userType 사용자 유형
     * @return 기본 언어
     */
    private String getDefaultLanguage(UserType userType) {
        return switch (userType) {
            case SELLER, BROKER -> "ko";
            case BUYER -> "en";
        };
    }
}
