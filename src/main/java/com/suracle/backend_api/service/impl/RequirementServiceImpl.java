package com.suracle.backend_api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suracle.backend_api.dto.requirement.RequirementAnalysisResponse;
import com.suracle.backend_api.service.RequirementService;
import com.suracle.backend_api.service.http.RequirementsApiClient;
import com.suracle.backend_api.service.util.EnglishNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequirementServiceImpl implements RequirementService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RequirementsApiClient apiClient;

    @Override
    public RequirementAnalysisResponse getRequirementAnalysis(Long productId) {
        // Map productId to a json file name. For now, use a simple convention.
        String fileName = String.format("requirements/product-%d.json", productId);
        ClassPathResource resource = new ClassPathResource(fileName);
        if (!resource.exists()) {
            log.warn("Requirement JSON not found for productId={}, file={}", productId, fileName);
            // Return an empty, invalid response instead of erroring
            return RequirementAnalysisResponse.builder()
                    .productId(String.valueOf(productId))
                    .isValid(false)
                    .confidenceScore(0.0)
                    .pendingAnalysis("No requirement JSON available")
                    .build();
        }
        try (InputStream is = resource.getInputStream()) {
            RequirementAnalysisResponse resp = objectMapper.readValue(is, RequirementAnalysisResponse.class);
            // ensure productId set
            if (resp.getProductId() == null || resp.getProductId().isEmpty()) {
                resp.setProductId(String.valueOf(productId));
            }
            // Optional enrichment: attempt minimal live calls without failing the request
            try {
                String english = EnglishNameUtil.toEnglishQuery(resp.getProductName());
                apiClient.callOpenFdaCosmeticEvent(english).ifPresent(json -> {
                    // noop: presence indicates reachable; future mapping can enrich fields
                });
                if (english != null && !english.isBlank()) {
                    apiClient.callEpaSrsChemname(english);
                }
                apiClient.checkCbpPortalReachable();
            } catch (Exception ignored) {
            }
            return resp;
        } catch (IOException e) {
            log.error("Failed to read requirement JSON for productId={}", productId, e);
            return RequirementAnalysisResponse.builder()
                    .productId(String.valueOf(productId))
                    .isValid(false)
                    .confidenceScore(0.0)
                    .pendingAnalysis("Failed to load requirement JSON")
                    .build();
        }
    }
}

/*
 Legacy DB-based implementation (kept for reference, not active):

// package com.suracle.backend_api.service;
//
// import com.suracle.backend_api.dto.requirement.RequirementAnalysisResponse;
// import com.suracle.backend_api.entity.cache.ProductAnalysisCache;
// import com.suracle.backend_api.entity.product.Product;
// import com.suracle.backend_api.repository.ProductAnalysisCacheRepository;
// import com.suracle.backend_api.repository.ProductRepository;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Service;
//
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;
//
// @Service
// @RequiredArgsConstructor
// @Slf4j
// public class RequirementService {
//
//     private final ProductAnalysisCacheRepository productAnalysisCacheRepository;
//     private final ProductRepository productRepository;
//
//     public RequirementAnalysisResponse getRequirementAnalysis(Long productId) {
//         log.info("Getting requirement analysis for product ID: {}", productId);
//
//         Optional<Product> productOpt = productRepository.findById(productId.intValue());
//         if (productOpt.isEmpty()) {
//             throw new RuntimeException("Product not found with ID: " + productId);
//         }
//
//         Product product = productOpt.get();
//
//         Optional<ProductAnalysisCache> cacheOpt = productAnalysisCacheRepository
//             .findByProductIdAndAnalysisType(productId.intValue(), "requirements");
//
//         if (cacheOpt.isEmpty()) {
//             log.warn("No requirement analysis cache found for product ID: {}", productId);
//             return createEmptyResponse(product);
//         }
//
//         ProductAnalysisCache cache = cacheOpt.get();
//
//         try {
//             return parseRequirementAnalysis(cache, product);
//         } catch (Exception e) {
//             log.error("Error parsing requirement analysis for product ID: {}", productId, e);
//             return createEmptyResponse(product);
//         }
//     }
//
//     private RequirementAnalysisResponse parseRequirementAnalysis(ProductAnalysisCache cache, Product product) {
//         try {
//             com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
//             com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(cache.getAnalysisResult());
//
//             return RequirementAnalysisResponse.builder()
//                 .productId(product.getProductId())
//                 .productName(product.getProductName())
//                 .hsCode(product.getHsCode())
//                 .criticalActions(parseStringArray(jsonNode.path("critical_actions")))
//                 .requiredDocuments(parseStringArray(jsonNode.path("required_documents")))
//                 .complianceSteps(parseStringArray(jsonNode.path("compliance_steps")))
//                 .timeline(jsonNode.path("timeline").asText(null))
//                 .brokerRejectionReason(jsonNode.path("broker_rejection_reason").asText(null))
//                 .criticalDeadline(jsonNode.path("critical_deadline").asText(null))
//                 .qualityStandards(jsonNode.path("quality_standards").asText(null))
//                 .coldChainRequirement(jsonNode.path("cold_chain_requirement").asText(null))
//                 .criticalWarning(jsonNode.path("critical_warning").asText(null))
//                 .pendingAnalysis(jsonNode.path("pending_analysis").asText(null))
//                 .sources(parseStringArrayFromSources(cache.getSources()))
//                 .confidenceScore(cache.getConfidenceScore().doubleValue())
//                 .isValid(cache.getIsValid())
//                 .lastUpdated(cache.getUpdatedAt().toString())
//                 .build();
//         } catch (Exception e) {
//             log.error("Error parsing requirement analysis JSON for product ID: {}", product.getId(), e);
//             return createEmptyResponse(product);
//         }
//     }
//
//     private List<String> parseStringArray(com.fasterxml.jackson.databind.JsonNode jsonNode) {
//         List<String> result = new ArrayList<>();
//         if (jsonNode.isArray()) {
//             for (com.fasterxml.jackson.databind.JsonNode node : jsonNode) {
//                 result.add(node.asText());
//             }
//         }
//         return result;
//     }
//
//     private List<String> parseStringArrayFromSources(String sourcesJson) {
//         List<String> result = new ArrayList<>();
//         try {
//             com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
//             com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(sourcesJson);
//             if (jsonNode.isArray()) {
//                 for (com.fasterxml.jackson.databind.JsonNode node : jsonNode) {
//                     result.add(node.asText());
//                 }
//             }
//         } catch (Exception e) {
//             log.error("Error parsing sources JSON: {}", sourcesJson, e);
//         }
//         return result;
//     }
//
//     private RequirementAnalysisResponse createEmptyResponse(Product product) {
//         return RequirementAnalysisResponse.builder()
//             .productId(product.getProductId())
//             .productName(product.getProductName())
//             .hsCode(product.getHsCode())
//             .criticalActions(new ArrayList<>())
//             .requiredDocuments(new ArrayList<>())
//             .complianceSteps(new ArrayList<>())
//             .timeline(null)
//             .brokerRejectionReason(null)
//             .criticalDeadline(null)
//             .qualityStandards(null)
//             .coldChainRequirement(null)
//             .criticalWarning(null)
//             .pendingAnalysis(null)
//             .sources(new ArrayList<>())
//             .confidenceScore(0.0)
//             .isValid(false)
//             .lastUpdated(null)
//             .build();
//     }
// }
*/



