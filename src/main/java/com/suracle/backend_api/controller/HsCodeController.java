package com.suracle.backend_api.controller;

import com.suracle.backend_api.dto.hs.HsCodeResponseDto;
import com.suracle.backend_api.service.HsCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hs-codes")
@RequiredArgsConstructor
@Slf4j
public class HsCodeController {

    private final HsCodeService hsCodeService;

    /**
     * HS 코드로 조회
     * @param hsCode HS 코드
     * @return HS 코드 정보
     */
    @GetMapping("/{hsCode}")
    public ResponseEntity<HsCodeResponseDto> getHsCodeByCode(@PathVariable String hsCode) {
        try {
            log.info("HS 코드 조회 요청 - HS 코드: {}", hsCode);
            HsCodeResponseDto response = hsCodeService.getHsCodeByCode(hsCode);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("HS 코드 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("HS 코드 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
