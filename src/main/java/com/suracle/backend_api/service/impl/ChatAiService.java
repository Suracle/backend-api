package com.suracle.backend_api.service.impl;

import com.suracle.backend_api.entity.chat.ChatSession;
import com.suracle.backend_api.entity.product.Product;
import com.suracle.backend_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 샘플 데이터 기반 AI 응답 서비스 (MVP용)
 * 추후 실제 AI 서비스로 대체 예정
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatAiService {

    private final ProductRepository productRepository;

    /**
     * 사용자 메시지에 대한 AI 응답 생성
     * @param session 채팅 세션
     * @param userMessage 사용자 메시지
     * @return AI 응답
     */
    public String generateResponse(ChatSession session, String userMessage) {
        log.info("AI 응답 생성 - 세션 타입: {}, 사용자 메시지: {}", session.getSessionType(), userMessage);
        
        String lowerMessage = userMessage.toLowerCase().trim();
        
        // 사용자 유형별 응답 분기
        switch (session.getSessionType()) {
            case SELLER_PRODUCT_INQUIRY:
                return generateSellerResponse(session, lowerMessage);
            case BUYER_PURCHASE_INQUIRY:
                return generateBuyerResponse(session, lowerMessage);
            default:
                return generateGeneralResponse(lowerMessage);
        }
    }

    /**
     * 판매자용 응답 생성
     */
    private String generateSellerResponse(ChatSession session, String userMessage) {
        // 상품명 검색
        if (containsProductKeywords(userMessage)) {
            String productName = extractProductName(userMessage);
            if (productName != null) {
                return generateProductInquiryResponse(productName, userMessage);
            }
        }
        
        // 일반적인 판매자 문의 응답
        if (userMessage.contains("안녕") || userMessage.contains("hello")) {
            return "안녕하세요! 판매자님의 상품 등록을 도와드리겠습니다. 어떤 상품에 대해 문의하시나요?";
        } else if (userMessage.contains("상품") || userMessage.contains("product")) {
            return "상품에 대한 문의를 도와드리겠습니다. 상품명을 알려주시면 HS코드, 관세, 요건, 판례 정보를 제공해드릴 수 있습니다.";
        } else if (userMessage.contains("관세") || userMessage.contains("tariff")) {
            return "관세 정보를 제공해드리겠습니다. 어떤 상품의 관세를 알고 싶으신가요?";
        } else if (userMessage.contains("요건") || userMessage.contains("requirement")) {
            return "수입 요건에 대해 안내해드리겠습니다. 구체적인 상품을 말씀해주세요.";
        } else if (userMessage.contains("판례") || userMessage.contains("precedent")) {
            return "관련 판례 정보를 제공해드리겠습니다. 어떤 상품의 판례를 찾고 계신가요?";
        } else {
            return "판매자님의 상품 등록을 도와드리겠습니다. 구체적인 질문을 해주시면 더 정확한 답변을 드릴 수 있습니다.";
        }
    }

    /**
     * 구매자용 응답 생성
     */
    private String generateBuyerResponse(ChatSession session, String userMessage) {
        // 상품명 검색
        if (containsProductKeywords(userMessage)) {
            String productName = extractProductName(userMessage);
            if (productName != null) {
                return generateBuyerProductResponse(productName, userMessage);
            }
        }
        
        // 일반적인 구매자 문의 응답
        if (userMessage.contains("안녕") || userMessage.contains("hello")) {
            return "Hello! I'm here to help you with your import inquiries. What product are you interested in?";
        } else if (userMessage.contains("product") || userMessage.contains("상품")) {
            return "I can help you with product information. Please tell me the product name you're interested in.";
        } else if (userMessage.contains("requirement") || userMessage.contains("요건")) {
            return "I can provide import requirements information. Which product do you want to know about?";
        } else if (userMessage.contains("tariff") || userMessage.contains("관세")) {
            return "I can help you calculate tariffs. Please specify the product and quantity.";
        } else {
            return "I'm here to help with your import inquiries. Please ask me about specific products or requirements.";
        }
    }

    /**
     * 일반 응답 생성
     */
    private String generateGeneralResponse(String userMessage) {
        if (userMessage.contains("안녕") || userMessage.contains("hello")) {
            return "안녕하세요! AI 무역 어시스턴트입니다. 어떤 도움이 필요하신가요?";
        } else {
            return "죄송합니다. 더 구체적인 질문을 해주시면 도움을 드릴 수 있습니다.";
        }
    }

    /**
     * 상품명이 포함된 키워드 확인
     */
    private boolean containsProductKeywords(String userMessage) {
        List<String> productKeywords = Arrays.asList(
            "premium vitamin c serum", "비타민c 세럼",
            "hydrating snail cream", "달팽이 크림",
            "premium red ginseng extract", "홍삼 추출액",
            "instant cooked rice", "즉석밥",
            "rice snack", "쌀 과자",
            "instant ramyeon", "즉석라면",
            "bbq seasoning", "바비큐 시즈닝",
            "kimchi", "김치",
            "perfume", "향수"
        );
        
        return productKeywords.stream().anyMatch(userMessage::contains);
    }

    /**
     * 사용자 메시지에서 상품명 추출
     */
    private String extractProductName(String userMessage) {
        // 샘플 데이터의 상품명들과 매칭
        if (userMessage.contains("premium vitamin c serum") || userMessage.contains("비타민c 세럼")) {
            return "Premium Vitamin C Serum";
        } else if (userMessage.contains("hydrating snail cream") || userMessage.contains("달팽이 크림")) {
            return "Hydrating Snail Cream";
        } else if (userMessage.contains("premium red ginseng extract") || userMessage.contains("홍삼 추출액")) {
            return "Premium Red Ginseng Extract";
        } else if (userMessage.contains("instant cooked rice") || userMessage.contains("즉석밥")) {
            return "Instant Cooked Rice Multipack";
        } else if (userMessage.contains("rice snack") || userMessage.contains("쌀 과자")) {
            return "Korean Rice Snack Assorted Pack";
        } else if (userMessage.contains("instant ramyeon") || userMessage.contains("즉석라면")) {
            return "Premium Instant Ramyeon 4-Pack";
        } else if (userMessage.contains("bbq seasoning") || userMessage.contains("바비큐 시즈닝")) {
            return "Korean BBQ Seasoning Mix";
        } else if (userMessage.contains("kimchi") || userMessage.contains("김치")) {
            return "Fermented Cabbage Kimchi";
        } else if (userMessage.contains("perfume") || userMessage.contains("향수")) {
            return "Rose Garden Perfume 50ml";
        }
        
        return null;
    }

    /**
     * 상품 문의 응답 생성
     */
    private String generateProductInquiryResponse(String productName, String userMessage) {
        // 실제 상품 검색
        Optional<Product> productOpt = productRepository.findByProductNameContainingIgnoreCase(productName);
        
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            
            if (userMessage.contains("관세") || userMessage.contains("tariff")) {
                return generateTariffResponse(product);
            } else if (userMessage.contains("요건") || userMessage.contains("requirement")) {
                return generateRequirementResponse(product);
            } else if (userMessage.contains("판례") || userMessage.contains("precedent")) {
                return generatePrecedentResponse(product);
            } else if (userMessage.contains("hs코드") || userMessage.contains("hs code")) {
                return generateHsCodeResponse(product);
            } else {
                return generateGeneralProductResponse(product);
            }
        } else {
            return "죄송합니다. '" + productName + "' 상품을 찾을 수 없습니다. 정확한 상품명을 확인해주세요.";
        }
    }

    /**
     * 구매자 상품 응답 생성
     */
    private String generateBuyerProductResponse(String productName, String userMessage) {
        Optional<Product> productOpt = productRepository.findByProductNameContainingIgnoreCase(productName);
        
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            
            if (userMessage.contains("requirement") || userMessage.contains("요건")) {
                return generateBuyerRequirementResponse(product);
            } else if (userMessage.contains("tariff") || userMessage.contains("관세")) {
                return generateBuyerTariffResponse(product);
            } else {
                return generateBuyerGeneralResponse(product);
            }
        } else {
            return "I couldn't find the product '" + productName + "'. Please check the product name and try again.";
        }
    }

    /**
     * 관세 응답 생성
     */
    private String generateTariffResponse(Product product) {
        return String.format("'%s' 상품의 관세 정보입니다:\n\n" +
                "• HS코드: %s\n" +
                "• 단가: $%.2f\n" +
                "• FOB 가격: $%.2f\n" +
                "• 관세율: 15%% (상호관세 적용)\n" +
                "• 1개 수입시 총 비용: $%.2f\n\n" +
                "상호관세로 인해 15%% 관세가 부과되므로 가격 경쟁력을 재검토하시기 바랍니다.",
                product.getProductName(),
                product.getHsCode(),
                product.getPrice(),
                product.getFobPrice(),
                product.getFobPrice().multiply(java.math.BigDecimal.valueOf(1.15)));
    }

    /**
     * 요건 응답 생성
     */
    private String generateRequirementResponse(Product product) {
        return String.format("'%s' 상품의 수입 요건입니다:\n\n" +
                "• FDA 등록 필수\n" +
                "• 성분 안전성 평가 필요\n" +
                "• 21 CFR 701 라벨링 준수\n" +
                "• VCRP 자발적 등록 권장\n\n" +
                "상세한 요건은 상품 유형에 따라 달라질 수 있습니다.",
                product.getProductName());
    }

    /**
     * 판례 응답 생성
     */
    private String generatePrecedentResponse(Product product) {
        return String.format("'%s' 상품의 관련 판례입니다:\n\n" +
                "• 2024년 4월 유사 제품 승인 사례\n" +
                "• 농도 검증 및 안정성 테스트 완료로 통과\n" +
                "• pH 테스트 및 자극성 테스트 중요\n\n" +
                "성분 농도는 반드시 HPLC 분석법으로 검증하시기 바랍니다.",
                product.getProductName());
    }

    /**
     * HS코드 응답 생성
     */
    private String generateHsCodeResponse(Product product) {
        return String.format("'%s' 상품의 HS코드 정보입니다:\n\n" +
                "• HS코드: %s\n" +
                "• 분류: 기타 미용 또는 메이크업 제품\n" +
                "• 설명: 안티에이징 페이셜 세럼\n\n" +
                "이 분류는 FDA 화장품 규정을 따릅니다.",
                product.getProductName(),
                product.getHsCode());
    }

    /**
     * 일반 상품 응답 생성
     */
    private String generateGeneralProductResponse(Product product) {
        return String.format("'%s' 상품 정보입니다:\n\n" +
                "• HS코드: %s\n" +
                "• 가격: $%.2f\n" +
                "• FOB 가격: $%.2f\n" +
                "• 원산지: %s\n\n" +
                "어떤 정보가 더 필요하신가요? (HS코드, 관세, 요건, 판례)",
                product.getProductName(),
                product.getHsCode(),
                product.getPrice(),
                product.getFobPrice(),
                product.getOriginCountry());
    }

    /**
     * 구매자 요건 응답 생성
     */
    private String generateBuyerRequirementResponse(Product product) {
        return String.format("For importing '%s' to the US:\n\n" +
                "• FDA Prior Notice required\n" +
                "• Facility registration needed\n" +
                "• Labeling compliance (21 CFR 701)\n" +
                "• Ingredient safety assessment\n\n" +
                "Please ensure all requirements are met before import.",
                product.getProductName());
    }

    /**
     * 구매자 관세 응답 생성
     */
    private String generateBuyerTariffResponse(Product product) {
        return String.format("Tariff information for '%s':\n\n" +
                "• HS Code: %s\n" +
                "• Unit Price: $%.2f\n" +
                "• FOB Price: $%.2f\n" +
                "• Tariff Rate: 15%% (retaliatory tariff)\n" +
                "• Total cost per unit: $%.2f\n\n" +
                "Consider the tariff impact on your pricing strategy.",
                product.getProductName(),
                product.getHsCode(),
                product.getPrice(),
                product.getFobPrice(),
                product.getFobPrice().multiply(java.math.BigDecimal.valueOf(1.15)));
    }

    /**
     * 구매자 일반 응답 생성
     */
    private String generateBuyerGeneralResponse(Product product) {
        return String.format("Product information for '%s':\n\n" +
                "• HS Code: %s\n" +
                "• Price: $%.2f\n" +
                "• FOB Price: $%.2f\n" +
                "• Origin: %s\n\n" +
                "What specific information do you need? (requirements, tariffs, precedents)",
                product.getProductName(),
                product.getHsCode(),
                product.getPrice(),
                product.getFobPrice(),
                product.getOriginCountry());
    }
}
