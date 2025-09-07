package com.suracle.backend_api.service;

import com.suracle.backend_api.dto.hs.HsCodeResponseDto;

public interface HsCodeService {
    
    /**
     * HS 코드로 조회
     * @param hsCode HS 코드
     * @return HS 코드 정보
     */
    HsCodeResponseDto getHsCodeByCode(String hsCode);
}
