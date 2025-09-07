package com.suracle.backend_api.service;

import com.suracle.backend_api.entity.product.Product;

public interface ProductAnalysisService {
    
    /**
     * 상품 등록 후 백그라운드 분석 큐에 추가
     * @param product 상품 정보
     */
    void scheduleBackgroundAnalysis(Product product);
    
    /**
     * HS코드 분석 완료 후 선택된 HS코드 저장
     * @param productId 상품 ID
     * @param selectedHsCode 선택된 HS코드
     * @param hsCodeDescription HS코드 설명
     */
    void saveSelectedHsCode(Integer productId, String selectedHsCode, String hsCodeDescription);
}
