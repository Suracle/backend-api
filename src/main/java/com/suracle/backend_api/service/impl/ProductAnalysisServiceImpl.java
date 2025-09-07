package com.suracle.backend_api.service.impl;

import com.suracle.backend_api.entity.cache.AnalysisQueue;
import com.suracle.backend_api.entity.product.Product;
import com.suracle.backend_api.repository.AnalysisQueueRepository;
import com.suracle.backend_api.repository.ProductRepository;
import com.suracle.backend_api.service.ProductAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductAnalysisServiceImpl implements ProductAnalysisService {

    private final AnalysisQueueRepository analysisQueueRepository;
    private final ProductRepository productRepository;

    @Override
    public void scheduleBackgroundAnalysis(Product product) {
        log.info("백그라운드 분석 스케줄링 - 상품ID: {}", product.getId());
        
        // 관세/요건/판례 분석을 위한 큐 항목 생성
        String analysisTypesJson = "[\"tariff_1qty\", \"tariff_10qty\", \"requirements\", \"precedents\"]";
        log.info("생성할 analysisTypes JSON: {}", analysisTypesJson);
        
        AnalysisQueue analysisQueue = AnalysisQueue.builder()
            .product(product)
            .analysisTypes(analysisTypesJson)
            .status(AnalysisQueue.QueueStatus.PENDING)
            .priority(1) // 일반 우선순위
            .scheduledAt(LocalDateTime.now())
            .retryCount(0)
            .build();
        
        analysisQueueRepository.save(analysisQueue);
        log.info("백그라운드 분석 큐에 추가 완료 - 상품ID: {}", product.getId());
    }

    @Override
    public void saveSelectedHsCode(Integer productId, String selectedHsCode, String hsCodeDescription) {
        log.info("선택된 HS코드 저장 - 상품ID: {}, HS코드: {}", productId, selectedHsCode);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + productId));
        
        // 상품의 HS코드 업데이트
        product.setHsCode(selectedHsCode);
        productRepository.save(product);
        
        log.info("HS코드 저장 완료 - 상품ID: {}, HS코드: {}", productId, selectedHsCode);
    }
}
