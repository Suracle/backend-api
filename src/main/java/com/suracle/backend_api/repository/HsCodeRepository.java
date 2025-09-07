package com.suracle.backend_api.repository;

import com.suracle.backend_api.entity.hs.HsCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HsCodeRepository extends JpaRepository<HsCode, String> {
    
    /**
     * HS 코드로 조회
     * @param hsCode HS 코드
     * @return HsCode 엔티티
     */
    Optional<HsCode> findByHsCode(String hsCode);
}
