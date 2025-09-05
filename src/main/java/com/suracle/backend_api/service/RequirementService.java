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
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(cache.getAnalysisResult());
            
            return RequirementAnalysisResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .hsCode(product.getHsCode())
                .fdaRegistration(jsonNode.path("fda_registration").asBoolean(false))
                .cosmeticFacilityRegistration(jsonNode.path("cosmetic_facility_registration").asBoolean(false))
                .ingredientSafety(jsonNode.path("ingredient_safety").asBoolean(false))
                .labelingCompliance(jsonNode.path("labeling_compliance").asBoolean(false))
                .colorAdditiveApproval(jsonNode.path("color_additive_approval").asBoolean(false))
                .safetyTesting(jsonNode.path("safety_testing").asBoolean(false))
                .phTesting(jsonNode.path("ph_testing").asBoolean(false))
                .sensitiveSkinTesting(jsonNode.path("sensitive_skin_testing").asBoolean(false))
                .uvSafety(jsonNode.path("uv_safety").asBoolean(false))
                .chemicalDisclosure(jsonNode.path("chemical_disclosure").asBoolean(false))
                .proteinComplexDisclosure(jsonNode.path("protein_complex_disclosure").asBoolean(false))
                .aminoAcidDocumentation(jsonNode.path("amino_acid_documentation").asBoolean(false))
                .hairStrengthClaims(jsonNode.path("hair_strength_claims").asBoolean(false))
                .snailExtractSafety(jsonNode.path("snail_extract_safety").asBoolean(false))
                .sulfateFreeClaim(jsonNode.path("sulfate_free_claim").asBoolean(false))
                .additionalDocs(parseStringArray(jsonNode.path("additional_docs")))
                .sources(parseStringArray(jsonNode.path("sources")))
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

    private RequirementAnalysisResponse createEmptyResponse(Product product) {
        return RequirementAnalysisResponse.builder()
            .productId(product.getProductId())
            .productName(product.getProductName())
            .hsCode(product.getHsCode())
            .fdaRegistration(false)
            .cosmeticFacilityRegistration(false)
            .ingredientSafety(false)
            .labelingCompliance(false)
            .colorAdditiveApproval(false)
            .safetyTesting(false)
            .phTesting(false)
            .sensitiveSkinTesting(false)
            .uvSafety(false)
            .chemicalDisclosure(false)
            .proteinComplexDisclosure(false)
            .aminoAcidDocumentation(false)
            .hairStrengthClaims(false)
            .snailExtractSafety(false)
            .sulfateFreeClaim(false)
            .additionalDocs(new ArrayList<>())
            .sources(new ArrayList<>())
            .confidenceScore(0.0)
            .isValid(false)
            .lastUpdated(null)
            .build();
    }
}
