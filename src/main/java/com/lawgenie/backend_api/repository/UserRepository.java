package com.lawgenie.backend_api.repository;

import com.lawgenie.backend_api.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    /**
     * 이메일로 사용자 조회
     * @param email 이메일
     * @return 사용자 정보
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 이메일 중복 확인
     * @param email 이메일
     * @return 존재 여부
     */
    boolean existsByEmail(String email);
}
