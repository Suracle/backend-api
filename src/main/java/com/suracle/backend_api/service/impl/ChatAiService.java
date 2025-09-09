package com.suracle.backend_api.service.impl;

import com.suracle.backend_api.entity.chat.ChatSession;
import com.suracle.backend_api.entity.chat.enums.MessageType;
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
     * AI 응답 결과를 담는 내부 클래스
     */
    public static class AiResponse {
        private final String content;
        private final MessageType messageType;
        private final String metadata;
        
        public AiResponse(String content, MessageType messageType, String metadata) {
            this.content = content;
            this.messageType = messageType;
            this.metadata = metadata;
        }
        
        public String getContent() { return content; }
        public MessageType getMessageType() { return messageType; }
        public String getMetadata() { return metadata; }
    }

    /**
     * 사용자 메시지에 대한 AI 응답 생성
     * @param session 채팅 세션
     * @param userMessage 사용자 메시지
     * @return AI 응답
     */
    public String generateResponse(ChatSession session, String userMessage) {
        log.info("AI 응답 생성 - 세션 타입: {}, 언어: {}, 사용자 메시지: {}", 
                session.getSessionType(), session.getLanguage(), userMessage);
        
        String lowerMessage = userMessage.toLowerCase().trim();
        
        // 언어별 응답 분기
        if ("en".equals(session.getLanguage())) {
            return generateEnglishResponse(session, lowerMessage).getContent();
        } else {
            return generateKoreanResponse(session, lowerMessage).getContent();
        }
    }
    
    /**
     * 사용자 메시지에 대한 AI 응답 생성 (메시지 타입 포함)
     * @param session 채팅 세션
     * @param userMessage 사용자 메시지
     * @return AI 응답 (메시지 타입과 메타데이터 포함)
     */
    public AiResponse generateResponseWithType(ChatSession session, String userMessage) {
        log.info("AI 응답 생성 - 세션 타입: {}, 언어: {}, 사용자 메시지: {}", 
                session.getSessionType(), session.getLanguage(), userMessage);
        
        String lowerMessage = userMessage.toLowerCase().trim();
        
        // 언어별 응답 분기
        if ("en".equals(session.getLanguage())) {
            return generateEnglishResponse(session, lowerMessage);
        } else {
            return generateKoreanResponse(session, lowerMessage);
        }
    }

    /**
     * 영어 응답 생성 (구매자용 - 요건과 판례만)
     */
    private AiResponse generateEnglishResponse(ChatSession session, String userMessage) {
        // 상품명 검색
        if (containsProductKeywords(userMessage)) {
            String productName = extractProductName(userMessage);
            if (productName != null) {
                return generateEnglishProductResponse(productName, userMessage);
            }
        }
        
        // 확인 응답 처리 (임시 해결책)
        if (userMessage.contains("yes") || userMessage.contains("correct") || userMessage.contains("right")) {
            return new AiResponse("What would you like to know about this product?", 
                                MessageType.BUTTON_GROUP, 
                                "{\"language\": \"en\", \"step\": \"query_type_selection\", \"options\": [\"Import Requirements\", \"Related Precedents\"]}");
        }
        
        // 버튼 클릭 응답 처리
        if (userMessage.contains("import requirements") || userMessage.contains("requirements")) {
            return new AiResponse(generateBuyerRequirementResponse(null) + "\n\nDo you have any other questions?", 
                                MessageType.BUTTON_GROUP, 
                                "{\"language\": \"en\", \"step\": \"additional_actions\", \"options\": [\"Import Requirements\", \"Related Precedents\", \"Start Over\"], \"sources\": [\"FDA_Prior_Notice_Rule\", \"21_CFR_1.276\", \"DSHEA_Requirements\"], \"confidence\": 0.99, \"product_inquiry_id\": 3}");
        } else if (userMessage.contains("related precedents") || userMessage.contains("precedents")) {
            return new AiResponse(generateEnglishPrecedentResponse(null) + "\n\nDo you have any other questions?", 
                                MessageType.BUTTON_GROUP, 
                                "{\"language\": \"en\", \"step\": \"additional_actions\", \"options\": [\"Import Requirements\", \"Related Precedents\", \"Start Over\"], \"sources\": [\"FDA_Import_Procedures\", \"CBP_Entry_Requirements\"], \"confidence\": 0.98, \"additional_info\": true}");
        }
        
        // 기본 응답
        return new AiResponse("Hello! How can I help you? Please type the product name.", 
                            MessageType.TEXT, 
                            "{\"language\": \"en\", \"step\": \"greeting\", \"user_type\": \"buyer\"}");
    }

    /**
     * 한국어 응답 생성 (판매자용)
     */
    private AiResponse generateKoreanResponse(ChatSession session, String userMessage) {
        // 상품명 검색
        if (containsProductKeywords(userMessage)) {
            String productName = extractProductName(userMessage);
            if (productName != null) {
                return generateKoreanProductResponse(productName, userMessage);
            }
        }
        
        // 초기 메뉴 처리
        if (userMessage.contains("기존 상품") || userMessage.contains("상품 문의")) {
            return new AiResponse("상품명을 입력해주세요.", 
                                MessageType.TEXT, 
                                "{\"language\": \"ko\", \"step\": \"product_identification\", \"input_type\": \"text\", \"selected\": \"existing_product\", \"user_type\": \"seller\"}");
        }
        
        // 확인 응답 처리 (임시 해결책)
        if (userMessage.contains("응") || userMessage.contains("맞아") || userMessage.contains("네") || userMessage.contains("yes")) {
            return new AiResponse("어떤 정보가 필요하신가요?", 
                                MessageType.BUTTON_GROUP, 
                                "{\"language\": \"ko\", \"step\": \"query_type_selection\", \"options\": [\"HS코드\", \"관세\", \"요건\", \"판례\"]}");
        }
        
        // 버튼 클릭 응답 처리
        if (userMessage.contains("hs코드") || userMessage.contains("hs code")) {
            return new AiResponse(generateHsCodeResponse(null) + " 다른 정보가 더 필요하신가요?", 
                                MessageType.BUTTON_GROUP, 
                                "{\"language\": \"ko\", \"step\": \"additional_actions\", \"options\": [\"HS코드\", \"관세\", \"요건\", \"판례\", \"처음으로\"], \"sources\": [\"HTSUS_Classification\"], \"confidence\": 0.95}");
        } else if (userMessage.contains("관세") || userMessage.contains("tariff")) {
            return new AiResponse(generateTariffResponse(null) + " 다른 정보가 더 필요하신가요?", 
                                MessageType.BUTTON_GROUP, 
                                "{\"language\": \"ko\", \"step\": \"additional_actions\", \"options\": [\"HS코드\", \"관세\", \"요건\", \"판례\", \"처음으로\"], \"sources\": [\"US_RETALIATORY_TARIFF\", \"HTSUS_3304.99.50.00\"], \"confidence\": 0.98, \"product_inquiry_id\": 1, \"tariff_applied\": 390.0, \"tariff_rate\": 0.15}");
        } else if (userMessage.contains("요건") || userMessage.contains("requirement")) {
            return new AiResponse(generateRequirementResponse(null) + " 다른 정보가 더 필요하신가요?", 
                                MessageType.BUTTON_GROUP, 
                                "{\"language\": \"ko\", \"step\": \"additional_actions\", \"options\": [\"HS코드\", \"관세\", \"요건\", \"판례\", \"처음으로\"], \"sources\": [\"FDA_Requirements\"], \"confidence\": 0.95}");
        } else if (userMessage.contains("판례") || userMessage.contains("precedent")) {
            return new AiResponse(generateKoreanPrecedentResponse(null) + " 다른 정보가 더 필요하신가요?", 
                                MessageType.BUTTON_GROUP, 
                                "{\"language\": \"ko\", \"step\": \"additional_actions\", \"options\": [\"HS코드\", \"관세\", \"요건\", \"판례\", \"처음으로\"], \"sources\": [\"USDA_BEEF_BAN\", \"CBP_Seizure_Records\"], \"confidence\": 0.99, \"product_inquiry_id\": 2, \"alert_level\": \"critical\"}");
        }
        
        // 기본 응답
        return new AiResponse("안녕하세요! 어떤 도움이 필요하신가요?", 
                            MessageType.BUTTON_GROUP, 
                            "{\"language\": \"ko\", \"step\": \"main_menu\", \"user_type\": \"seller\", \"options\": [\"기존 상품 문의\"]}");
    }


    /**
     * 상품명이 포함된 키워드 확인
     */
    private boolean containsProductKeywords(String userMessage) {
        List<String> productKeywords = Arrays.asList(
            "premium vitamin c serum", "비타민c 세럼", "serum", "vitamin c",
            "hydrating snail cream", "달팽이 크림", "snail cream",
            "premium red ginseng extract", "홍삼 추출액", "ginseng", "red ginseng",
            "instant cooked rice", "즉석밥", "rice",
            "rice snack", "쌀 과자", "snack",
            "instant ramyeon", "즉석라면", "ramyeon", "ramen",
            "bbq seasoning", "바비큐 시즈닝", "seasoning",
            "kimchi", "김치",
            "perfume", "향수"
        );
        
        return productKeywords.stream().anyMatch(userMessage::contains);
    }

    /**
     * 사용자 메시지에서 상품명 추출
     */
    private String extractProductName(String userMessage) {
        // 샘플 데이터의 상품명들과 매칭 (부분 매칭 지원)
        if (userMessage.contains("premium vitamin c serum") || userMessage.contains("비타민c 세럼") || 
            userMessage.contains("serum") || userMessage.contains("vitamin c")) {
            return "Premium Vitamin C Serum";
        } else if (userMessage.contains("hydrating snail cream") || userMessage.contains("달팽이 크림") || 
                   userMessage.contains("snail cream")) {
            return "Hydrating Snail Cream";
        } else if (userMessage.contains("premium red ginseng extract") || userMessage.contains("홍삼 추출액") || 
                   userMessage.contains("ginseng") || userMessage.contains("red ginseng")) {
            return "Premium Red Ginseng Extract";
        } else if (userMessage.contains("instant cooked rice") || userMessage.contains("즉석밥") || 
                   userMessage.contains("rice")) {
            return "Instant Cooked Rice Multipack";
        } else if (userMessage.contains("rice snack") || userMessage.contains("쌀 과자") || 
                   userMessage.contains("snack")) {
            return "Korean Rice Snack Assorted Pack";
        } else if (userMessage.contains("instant ramyeon") || userMessage.contains("즉석라면") || 
                   userMessage.contains("ramyeon") || userMessage.contains("ramen")) {
            return "Premium Instant Ramyeon 4-Pack";
        } else if (userMessage.contains("bbq seasoning") || userMessage.contains("바비큐 시즈닝") || 
                   userMessage.contains("seasoning")) {
            return "Korean BBQ Seasoning Mix";
        } else if (userMessage.contains("kimchi") || userMessage.contains("김치")) {
            return "Fermented Cabbage Kimchi";
        } else if (userMessage.contains("perfume") || userMessage.contains("향수")) {
            return "Rose Garden Perfume 50ml";
        }
        
        return null;
    }


    /**
     * 관세 응답 생성 (data.sql Session 1과 동일)
     */
    private String generateTariffResponse(Product product) {
        if (product != null && (product.getProductName().contains("Vitamin C") || product.getProductName().contains("비타민"))) {
            return "비타민C 세럼(HS 3304.99.50.00)은 현재 상호관세로 15%가 부과됩니다. 50개 수출시 총 FOB 가격 $2,600, 관세 $390이 추가되어 총 수입비용은 $2,990입니다. 화장품이므로 FDA 등록, 성분 안전성 평가, 21 CFR 701 라벨링 준수가 필요합니다.";
        } else {
            String productName = product != null ? product.getProductName() : "상품";
            String hsCode = product != null ? product.getHsCode() : "N/A";
            String price = product != null ? product.getPrice().toString() : "N/A";
            String fobPrice = product != null ? product.getFobPrice().toString() : "N/A";
            String totalCost = product != null ? product.getFobPrice().multiply(java.math.BigDecimal.valueOf(1.15)).toString() : "N/A";
            
            return String.format("'%s' 상품의 관세 정보입니다:\n\n" +
                    "• HS코드: %s\n" +
                    "• 단가: $%s\n" +
                    "• FOB 가격: $%s\n" +
                    "• 관세율: 15%% (상호관세 적용)\n" +
                    "• 1개 수입시 총 비용: $%s\n\n" +
                    "상호관세로 인해 15%% 관세가 부과되므로 가격 경쟁력을 재검토하시기 바랍니다.",
                    productName, hsCode, price, fobPrice, totalCost);
        }
    }

    /**
     * 요건 응답 생성
     */
    private String generateRequirementResponse(Product product) {
        String productName = product != null ? product.getProductName() : "상품";
        return String.format("'%s' 상품의 수입 요건입니다:\n\n" +
                "• FDA 등록 필수\n" +
                "• 성분 안전성 평가 필요\n" +
                "• 21 CFR 701 라벨링 준수\n" +
                "• VCRP 자발적 등록 권장\n\n" +
                "상세한 요건은 상품 유형에 따라 달라질 수 있습니다.",
                productName);
    }


    /**
     * HS코드 응답 생성
     */
    private String generateHsCodeResponse(Product product) {
        String productName = product != null ? product.getProductName() : "상품";
        String hsCode = product != null ? product.getHsCode() : "N/A";
        return String.format("'%s' 상품의 HS코드 정보입니다:\n\n" +
                "• HS코드: %s\n" +
                "• 분류: 기타 미용 또는 메이크업 제품\n" +
                "• 설명: 안티에이징 페이셜 세럼\n\n" +
                "이 분류는 FDA 화장품 규정을 따릅니다.",
                productName, hsCode);
    }


    /**
     * 구매자 요건 응답 생성 (data.sql Session 3과 동일)
     */
    private String generateBuyerRequirementResponse(Product product) {
        if (product != null && (product.getProductName().contains("Red Ginseng") || product.getProductName().contains("홍삼"))) {
            return "For importing " + product.getProductName() + " to the US, you need:\n\n" +
                   "**Critical Requirement - FDA Prior Notice:**\n" +
                   "Since May 6, 2009, FDA requires Prior Notice for ALL food imports (including dietary supplements) unless specifically exempted. You must submit Prior Notice before the shipment arrives.\n\n" +
                   "**Exemptions:** Only personal use (not for sale/distribution) to yourself, friends, or family.\n\n" +
                   "**Other Requirements:**\n" +
                   "1) FDA facility registration for manufacturer\n" +
                   "2) DSHEA compliance for dietary supplements\n" +
                   "3) cGMP requirements (21 CFR 111)\n" +
                   "4) Nutritional supplement labeling\n" +
                   "5) Health claims substantiation\n" +
                   "6) Ginsenoside content verification\n\n" +
                   "**Important:** Commercial imports without Prior Notice will be refused entry.";
        } else {
            String productName = product != null ? product.getProductName() : "this product";
            return String.format("For importing '%s' to the US:\n\n" +
                    "• FDA Prior Notice required\n" +
                    "• Facility registration needed\n" +
                    "• Labeling compliance (21 CFR 701)\n" +
                    "• Ingredient safety assessment\n\n" +
                    "Please ensure all requirements are met before import.",
                    productName);
        }
    }



    /**
     * 영어 상품 응답 생성 (구매자용 - 요건과 판례만)
     */
    private AiResponse generateEnglishProductResponse(String productName, String userMessage) {
        Optional<Product> productOpt = productRepository.findByProductNameContainingIgnoreCase(productName);
        
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            
            // 상품 확인 단계
            if (userMessage.contains("yes") || userMessage.contains("correct") || userMessage.contains("right")) {
                return new AiResponse("What would you like to know about this product?", 
                                    MessageType.BUTTON_GROUP, 
                                    "{\"language\": \"en\", \"step\": \"query_type_selection\", \"options\": [\"Import Requirements\", \"Related Precedents\"], \"product_id\": \"" + product.getId() + "\"}");
            } else if (userMessage.contains("no") || userMessage.contains("wrong") || userMessage.contains("incorrect")) {
                return new AiResponse("I apologize for the confusion. Could you please provide the correct product name?", 
                                    MessageType.TEXT, 
                                    "{\"language\": \"en\", \"step\": \"clarification_request\"}");
            } else if (userMessage.contains("requirement") || userMessage.contains("요건")) {
                return new AiResponse(generateBuyerRequirementResponse(product), 
                                    MessageType.TEXT, 
                                    "{\"language\": \"en\", \"sources\": [\"FDA_Prior_Notice_Rule\", \"21_CFR_1.276\", \"DSHEA_Requirements\"], \"confidence\": 0.99, \"product_inquiry_id\": 3}");
            } else if (userMessage.contains("precedent") || userMessage.contains("판례")) {
                return new AiResponse(generateEnglishPrecedentResponse(product), 
                                    MessageType.TEXT, 
                                    "{\"language\": \"en\", \"sources\": [\"FDA_Import_Procedures\", \"CBP_Entry_Requirements\"], \"confidence\": 0.98, \"additional_info\": true}");
            } else {
                // 상품 확인 요청 (data.sql의 Session 3과 동일)
                return new AiResponse("I found " + product.getProductName() + " in our catalog. This is a 6-year aged Korean red ginseng concentrated extract with 80mg ginsenosides per serving. Is this the product you are asking about?", 
                                    MessageType.TEXT, 
                                    "{\"language\": \"en\", \"step\": \"product_confirmation\", \"product_id\": \"" + product.getId() + "\", \"product_name\": \"" + product.getProductName() + "\"}");
            }
        } else {
            return new AiResponse("I could not find \"" + productName + "\" in our current product catalog. Please try entering a different product name or check the spelling.", 
                                MessageType.TEXT, 
                                "{\"language\": \"en\", \"step\": \"product_not_found\", \"error_type\": \"product_not_in_catalog\"}");
        }
    }

    /**
     * 한국어 상품 응답 생성 (판매자용)
     */
    private AiResponse generateKoreanProductResponse(String productName, String userMessage) {
        Optional<Product> productOpt = productRepository.findByProductNameContainingIgnoreCase(productName);
        
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            
            // 상품 확인 단계
            if (userMessage.contains("응") || userMessage.contains("맞아") || userMessage.contains("네") || userMessage.contains("yes")) {
                return new AiResponse("어떤 정보가 필요하신가요?", 
                                    MessageType.BUTTON_GROUP, 
                                    "{\"language\": \"ko\", \"step\": \"query_type_selection\", \"options\": [\"HS코드\", \"관세\", \"요건\", \"판례\"], \"product_id\": \"" + product.getId() + "\"}");
            } else if (userMessage.contains("아니") || userMessage.contains("아닌") || userMessage.contains("틀렸") || userMessage.contains("no")) {
                return new AiResponse("죄송합니다. 정확한 상품명을 다시 알려주세요.", 
                                    MessageType.TEXT, 
                                    "{\"language\": \"ko\", \"step\": \"clarification_request\"}");
            } else if (userMessage.contains("관세") || userMessage.contains("tariff")) {
                return new AiResponse(generateTariffResponse(product), 
                                    MessageType.TEXT, 
                                    "{\"language\": \"ko\", \"sources\": [\"US_RETALIATORY_TARIFF\", \"HTSUS_3304.99.50.00\"], \"confidence\": 0.98, \"product_inquiry_id\": 1, \"tariff_applied\": 390.0, \"tariff_rate\": 0.15}");
            } else if (userMessage.contains("요건") || userMessage.contains("requirement")) {
                return new AiResponse(generateRequirementResponse(product), 
                                    MessageType.TEXT, 
                                    "{\"language\": \"ko\", \"sources\": [\"FDA_Requirements\"], \"confidence\": 0.95}");
            } else if (userMessage.contains("판례") || userMessage.contains("precedent")) {
                return new AiResponse(generateKoreanPrecedentResponse(product), 
                                    MessageType.TEXT, 
                                    "{\"language\": \"ko\", \"sources\": [\"USDA_BEEF_BAN\", \"CBP_Seizure_Records\"], \"confidence\": 0.99, \"product_inquiry_id\": 2, \"alert_level\": \"critical\"}");
            } else if (userMessage.contains("hs코드") || userMessage.contains("hs code")) {
                return new AiResponse(generateHsCodeResponse(product), 
                                    MessageType.TEXT, 
                                    "{\"language\": \"ko\", \"sources\": [\"HTSUS_Classification\"], \"confidence\": 0.95}");
            } else {
                // 상품 확인 요청 (data.sql의 Session 1, 2와 동일)
                if (product.getProductName().contains("Vitamin C") || product.getProductName().contains("비타민")) {
                    return new AiResponse(product.getProductName() + "이 맞나요? 20% L-Ascorbic Acid 함유 안티에이징 세럼이군요.", 
                                        MessageType.TEXT, 
                                        "{\"language\": \"ko\", \"step\": \"product_confirmation\", \"product_id\": \"" + product.getId() + "\", \"product_name\": \"" + product.getProductName() + "\"}");
                } else {
                    return new AiResponse(product.getProductName() + "이 맞나요?", 
                                        MessageType.TEXT, 
                                        "{\"language\": \"ko\", \"step\": \"product_confirmation\", \"product_id\": \"" + product.getId() + "\", \"product_name\": \"" + product.getProductName() + "\"}");
                }
            }
        } else {
            return new AiResponse("죄송합니다. '" + productName + "' 상품을 찾을 수 없습니다. 정확한 상품명을 확인해주세요.", 
                                MessageType.TEXT, 
                                "{\"language\": \"ko\", \"step\": \"product_not_found\", \"error_type\": \"product_not_in_catalog\"}");
        }
    }

    /**
     * 한국어 판례 응답 생성
     */
    private String generateKoreanPrecedentResponse(Product product) {
        String productName = product != null ? product.getProductName() : "상품";
        
        // 상품별 구체적인 판례 정보 제공
        if (product != null && (product.getProductName().contains("Instant Ramyeon") || product.getProductName().contains("라면"))) {
            return "이 라면에 소고기 추출물이 포함된 스프가 있다면, 미국은 한국산 소고기 제품 수입을 금지하고 있어, 소고기 스프가 포함된 라면은 100% 적발 시 폐기처분됩니다. 유사 제품들이 모두 통관 거부되었습니다.";
        } else if (product != null && (product.getProductName().contains("Vitamin C") || product.getProductName().contains("비타민"))) {
            return "'" + productName + "' 상품의 관련 판례입니다:\n\n" +
                   "• 2024년 4월 유사 제품 승인 사례\n" +
                   "• 농도 검증 및 안정성 테스트 완료로 통과\n" +
                   "• pH 테스트 및 자극성 테스트 중요\n\n" +
                   "성분 농도는 반드시 HPLC 분석법으로 검증하시기 바랍니다.";
        } else {
            return "'" + productName + "' 상품의 관련 판례입니다:\n\n" +
                   "• 2024년 4월 유사 제품 승인 사례\n" +
                   "• 농도 검증 및 안정성 테스트 완료로 통과\n" +
                   "• pH 테스트 및 자극성 테스트 중요\n\n" +
                   "성분 농도는 반드시 HPLC 분석법으로 검증하시기 바랍니다.";
        }
    }

    /**
     * 영어 판례 응답 생성 (data.sql Session 3과 동일)
     */
    private String generateEnglishPrecedentResponse(Product product) {
        String productName = product != null ? product.getProductName() : "this product";
        
        if (product != null && (product.getProductName().contains("Red Ginseng") || product.getProductName().contains("홍삼"))) {
            return "Precedent information for " + productName + ":\n\n" +
                   "• **2024 May:** Similar ginseng capsule approval - ginsenoside content verification and cGMP certification led to successful approval\n" +
                   "• **2024 March:** Ginseng tea approval - Prior Notice completion and appropriate health functionality expression resulted in approval\n\n" +
                   "**Key Insights:**\n" +
                   "• Ginsenoside content must be verified using HPLC analysis method\n" +
                   "• Health functionality expressions should use FDA-approved terms like \"supports immune system\"\n" +
                   "• Prior Notice must be submitted within required timeframe (air transport: minimum 4 hours, water transport: minimum 8 hours)\n\n" +
                   "**Risk Factors:**\n" +
                   "• Automatic entry refusal if Prior Notice is missing\n" +
                   "• Health functionality overstatement leads to labeling violations";
        } else {
            return String.format("Precedent information for '%s':\n\n" +
                    "• 2024 April: Similar product approval case\n" +
                    "• Concentration verification and stability testing completed successfully\n" +
                    "• pH testing and irritation testing are important\n\n" +
                    "Component concentration must be verified using HPLC analysis method.",
                    productName);
        }
    }

}
