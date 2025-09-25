package com.suracle.backend_api.service;

import com.suracle.backend_api.dto.requirement.RequirementAnalysisResponse;
import com.suracle.backend_api.entity.cache.ProductAnalysisCache;
import com.suracle.backend_api.entity.product.Product;
import com.suracle.backend_api.repository.ProductAnalysisCacheRepository;
import com.suracle.backend_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequirementService {

    private final ProductAnalysisCacheRepository productAnalysisCacheRepository;
    private final ProductRepository productRepository;

    public RequirementAnalysisResponse getRequirementAnalysis(Long productId) {
        log.info("Getting requirement analysis for product ID: {}", productId);
        
        // 상품 정보 조회
        Optional<Product> productOpt = productRepository.findById(productId.intValue());
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found with ID: " + productId);
        }
        
        Product product = productOpt.get();
        
        // 캐시에서 requirement 분석 결과 조회
        Optional<ProductAnalysisCache> cacheOpt = productAnalysisCacheRepository
            .findByProductIdAndAnalysisType(productId.intValue(), "requirements");
        
        if (cacheOpt.isEmpty()) {
            log.warn("No requirement analysis cache found for product ID: {}", productId);
            return createEmptyResponse(product);
        }
        
        ProductAnalysisCache cache = cacheOpt.get();
        
        try {
            // JSON 파싱하여 RequirementAnalysisResponse 생성
            return parseRequirementAnalysis(cache, product);
        } catch (Exception e) {
            log.error("Error parsing requirement analysis for product ID: {}", productId, e);
            return createEmptyResponse(product);
        }
    }

    private RequirementAnalysisResponse parseRequirementAnalysis(ProductAnalysisCache cache, Product product) {
        try {
            // JSON 파싱을 위한 ObjectMapper 사용 (Jackson)
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = cache.getAnalysisResult();
            
            return RequirementAnalysisResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .hsCode(product.getHsCode())
                .criticalActions(parseStringArray(jsonNode.path("critical_actions")))
                .requiredDocuments(parseStringArray(jsonNode.path("required_documents")))
                .complianceSteps(parseStringArray(jsonNode.path("compliance_steps")))
                .timeline(jsonNode.path("timeline").asText(null))
                .brokerRejectionReason(jsonNode.path("broker_rejection_reason").asText(null))
                .criticalDeadline(jsonNode.path("critical_deadline").asText(null))
                .qualityStandards(jsonNode.path("quality_standards").asText(null))
                .coldChainRequirement(jsonNode.path("cold_chain_requirement").asText(null))
                .criticalWarning(jsonNode.path("critical_warning").asText(null))
                .pendingAnalysis(jsonNode.path("pending_analysis").asText(null))
                .sources(parseStringArrayFromSources(cache.getSources().toString()))
                .confidenceScore(cache.getConfidenceScore().doubleValue())
                .isValid(cache.getIsValid())
                .lastUpdated(cache.getUpdatedAt().toString())
                .build();
        } catch (Exception e) {
            log.error("Error parsing requirement analysis JSON for product ID: {}", product.getId(), e);
            return createEmptyResponse(product);
        }
    }

    private List<String> parseStringArray(com.fasterxml.jackson.databind.JsonNode jsonNode) {
        List<String> result = new ArrayList<>();
        if (jsonNode.isArray()) {
            for (com.fasterxml.jackson.databind.JsonNode node : jsonNode) {
                result.add(node.asText());
            }
        }
        return result;
    }

    private List<String> parseStringArrayFromSources(String sourcesJson) {
        List<String> result = new ArrayList<>();
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(sourcesJson);
            if (jsonNode.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode node : jsonNode) {
                    result.add(node.asText());
                }
            }
        } catch (Exception e) {
            log.error("Error parsing sources JSON: {}", sourcesJson, e);
        }
        return result;
    }

    private RequirementAnalysisResponse createEmptyResponse(Product product) {
        return RequirementAnalysisResponse.builder()
            .productId(product.getProductId())
            .productName(product.getProductName())
            .hsCode(product.getHsCode())
            .criticalActions(new ArrayList<>())
            .requiredDocuments(new ArrayList<>())
            .complianceSteps(new ArrayList<>())
            .timeline(null)
            .brokerRejectionReason(null)
            .criticalDeadline(null)
            .qualityStandards(null)
            .coldChainRequirement(null)
            .criticalWarning(null)
            .pendingAnalysis(null)
            .sources(new ArrayList<>())
            .confidenceScore(0.0)
            .isValid(false)
            .lastUpdated(null)
            .build();
    }
}
