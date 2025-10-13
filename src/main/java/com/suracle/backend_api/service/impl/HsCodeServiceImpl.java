package com.suracle.backend_api.service.impl;

import com.suracle.backend_api.dto.hs.HsCodeResponseDto;
import com.suracle.backend_api.entity.hs.HsCode;
import com.suracle.backend_api.repository.HsCodeRepository;
import com.suracle.backend_api.service.HsCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HsCodeServiceImpl implements HsCodeService {

    private final HsCodeRepository hsCodeRepository;

    @Override
    public HsCodeResponseDto getHsCodeByCode(String hsCode) {
        log.info("HS 코드 조회 요청 - HS 코드: {}", hsCode);

        HsCode hsCodeEntity = hsCodeRepository.findByHsCode(hsCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 HS 코드입니다: " + hsCode));

        return convertToHsCodeResponseDto(hsCodeEntity);
    }

    /**
     * HsCode 엔티티를 HsCodeResponseDto로 변환
     */
    private HsCodeResponseDto convertToHsCodeResponseDto(HsCode hsCode) {
        return HsCodeResponseDto.builder()
                .hsCode(hsCode.getHsCode())
                .description(hsCode.getDescription())
                .usTariffRate(hsCode.getUsTariffRate())
                .reasoning(hsCode.getReasoning())
                .tariffReasoning(hsCode.getTariffReasoning())  // 관세율 적용 근거 추가
                .lastUpdated(hsCode.getLastUpdated())
                .build();
    }
}
