package com.suracle.backend_api.service;

import com.suracle.backend_api.entity.product.Product;
import com.suracle.backend_api.repository.ProductRepository;
import com.suracle.backend_api.repository.ProductAnalysisCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suracle.backend_api.entity.cache.ProductAnalysisCache;
import com.suracle.backend_api.service.AiWorkflowService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductStartupAnalysisService {

    private final ProductRepository productRepository;
    private final ProductAnalysisCacheRepository productAnalysisCacheRepository;
    private final ProductService productService;
    private final AiWorkflowService aiWorkflowService;

    /**
     * 서버 시작 시 실행되는 메서드
     * HS코드가 있지만 requirements 분석이 없는 상품들에 대해 자동 분석 실행
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("🚀 애플리케이션 시작 완료 - 기존 상품 분석 상태 확인 시작");
        
        // 비동기로 실행하여 서버 시작 속도에 영향 없도록 함
        CompletableFuture.runAsync(() -> {
            try {
                checkAndAnalyzeMissingRequirements();
            } catch (Exception e) {
                log.error("❌ 서버 시작 시 상품 분석 상태 확인 중 오류 발생", e);
            }
        });
    }

    /**
     * HS코드가 있지만 requirements 분석이 누락된 상품들을 찾아서 분석 실행
     */
    @Transactional(readOnly = true)
    protected void checkAndAnalyzeMissingRequirements() {
        log.info("📋 HS코드가 있지만 requirements 분석이 누락된 상품들 확인 중...");
        
        try {
            // 모든 활성 상품 조회 (HS코드가 있는 것만)
            List<Product> productsWithHsCode = productRepository.findByActiveAndHsCodeNotNull();
            
            if (productsWithHsCode == null || productsWithHsCode.isEmpty()) {
                log.info("✅ HS코드가 있는 상품이 없습니다.");
                return;
            }

            int totalProducts = productsWithHsCode.size();
            int analyzedCount = 0;
            int skippedCount = 0;
            int errorCount = 0;

            for (Product product : productsWithHsCode) {
                try {
                    if (hasRequirementsAnalysisCache(product)) {
                        log.debug("⏭️ 요구사항 분석 캐시 존재 - 상품 ID: {}", product.getProductId());
                        skippedCount++;
                        continue;
                    }

                    log.info("🔍 요구사항 분석 필요한 상품 발견 - 상품 ID: {}, HS코드: {}", 
                            product.getProductId(), product.getHsCode());
                    
                    // 백그라운드에서 requirements 분석 실행
                    CompletableFuture.runAsync(() -> executeRequirementsAnalysis(product));
                    analyzedCount++;

                } catch (Exception e) {
                    log.error("❌ 상품 ID {} 분석 중 오류: {}", product.getProductId(), e.getMessage(), e);
                    errorCount++;
                }
            }

            log.info("📊 상품 분석 상태 확인 완료 - 총상품: {}, 분석완료: {}, 스킵: {}, 오류: {}", 
                    totalProducts, analyzedCount, skippedCount, errorCount);

        } catch (Exception e) {
            log.error("❌ 상품 분석 상태 확인 실패", e);
        }
    }

    /**
     * 상품에 requirements 분석 캐시가 있는지 확인
     */
    private boolean hasRequirementsAnalysisCache(Product product) {
        return productAnalysisCacheRepository.findByProductIdAndAnalysisType(product.getId(), "requirements").isPresent();
    }

    /**
     * requirements 분석 실행
     */
    private void executeRequirementsAnalysis(Product product) {
        try {
            log.info("🔬 요구사항 분석 실행 시작 - 상품 ID: {}, HS코드: {}", 
                    product.getProductId(), product.getHsCode());

            // AI 요구사항 분석 실행
            Map<String, Object> analysisResult = aiWorkflowService.executeRequirementsAnalysis(product);

            // 결과 저장 (요구사항 분석 전용 메서드 사용)
            productService.saveRequirementsAnalysisResult(product, analysisResult);

            log.info("✅ 요구사항 분석 완료 및 저장 - 상품 ID: {}", product.getProductId());

        } catch (Exception e) {
            log.error("❌ 상품 ID {} 요구사항 분석 실패: {}", product.getProductId(), e.getMessage(), e);
        }
    }
}
