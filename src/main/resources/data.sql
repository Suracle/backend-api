-- 1. 사용자(User) 데이터
INSERT INTO users (email, password, user_type, user_name, preferred_language, is_active, created_at, updated_at) VALUES
('buyer1@globalcosmetics.com', 'password123', 'BUYER', 'Emily Johnson', 'en', true, NOW(), NOW()),
('seller1@kbeauty.co.kr', 'password123', 'SELLER', '김민수', 'ko', true, NOW(), NOW()),
('broker1@tradepartners.com', 'password123', 'BROKER', '박상현', 'ko', true, NOW(), NOW());

-- 2. HS 코드(HsCode) 데이터 - 화장품 관련 실제 10자리 HTSUS 코드
INSERT INTO hs_codes (hs_code, description, us_tariff_rate, requirements, trade_agreements, last_updated, created_at, updated_at) VALUES
('3304.10.00.00', 'Lip makeup preparations', 0.0000, 'FDA cosmetic registration required, CPSC compliance for packaging', '{"KORUS": "duty-free", "USMCA": "preferential", "GSP": "eligible"}', NOW(), NOW(), NOW()),
('3304.20.00.00', 'Eyeion makeup preparations', 0.0000, 'FDA cosmetic registration, color additive approval if applicable', '{"KORUS": "duty-free", "USMCA": "preferential"}', NOW(), NOW(), NOW()),
('3304.30.00.00', 'Manicure or pedicure preparations', 0.0000, 'FDA cosmetic registration, CPSC child-resistant packaging if toxic', '{"KORUS": "duty-free", "USMCA": "preferential"}', NOW(), NOW(), NOW()),
('3304.91.00.00', 'Face powders', 0.0000, 'FDA cosmetic registration required', '{"KORUS": "duty-free", "USMCA": "preferential"}', NOW(), NOW(), NOW()),
('3304.99.50.00', 'Other beauty or makeup preparations and preparations for the care of the skin', 0.0000, 'FDA cosmetic registration, ingredient safety assessment', '{"KORUS": "duty-free", "USMCA": "preferential", "GSP": "eligible"}', NOW(), NOW(), NOW()),
('3305.10.00.00', 'Shampoos', 0.0000, 'FDA cosmetic registration required', '{"KORUS": "duty-free", "USMCA": "preferential"}', NOW(), NOW(), NOW()),
('3305.20.00.00', 'Hair preparats for permanent waving or straightening', 0.0500, 'FDA cosmetic registration, chemical safety data sheet', '{"KORUS": "duty-free", "USMCA": "preferential"}', NOW(), NOW(), NOW()),
('3305.30.00.00', 'Hair lacquers', 0.0500, 'FDA cosmetic registration, VOC compliance', '{"KORUS": "duty-free", "USMCA": "preferential"}', NOW(), NOW(), NOW()),
('3305.90.00.00', 'Other hair preparations', 0.0000, 'FDA cosmetic registration required', '{"KORUS": "duty-free", "USMCA": "preferential"}', NOW(), NOW(), NOW()),
('3307.41.00.00', 'Agarbatti and other odoriferous preparations which operate by burning', 0.0500, 'FDA cosmetic registration, fire safety compliance', '{"KORUS": "duty-free", "USMCA": "preferential"}', NOW(), NOW(), NOW()),
('3307.49.00.00', 'Other preparations for perfuming or deodorizing rooms', 0.0500, 'FDA cosmetic registration, VOC compliance', '{"KORUS": "duty-free", "USMCA": "preferential"}', NOW(), NOW(), NOW()),
('3307.90.00.00', 'Other perfumes and toilet waters', 0.0000, 'FDA cosmetic registration required', '{"KORUS": "duty-free", "USMCA": "preferential"}', NOW(), NOW(), NOW());

-- 3. 상품(Product) 데이터 - SELLER ID는 2번 사용자(김민수)
INSERT INTO products (seller_id, product_id, product_name, description, price, fob_price, origin_country, hs_code, status, is_active, created_at, updated_at) VALUES
(2, 'PROD-2024-001', 'Premium Vitamin C Serum', 
'High-concentration Vitamin C serum effective for skin tone improvement and anti-aging. Contains 20% Vitamin C and hyaluronic acid, suitable for all skin types. Volume: 30ml, glass bottle packaging, net weight: 50g. Use after toner in morning and evening skincare routine.', 
45.00, 52.00, 'KOR', '3304.99.50.00', 'APPROVED', true, NOW(), NOW()),
(2, 'PROD-2024-002', 'Matte Long-lasting Lipstick', 
'24-hour lasting matte lipstick providing vivid color and comfortable wear. Available in 5 red-toned shades. Creamy texture that does not dry out lips, suitable for both oily and dry skin. Volume: 3.5g, plastic case, total weight: 15g. Apply directly after lip balm.', 
22.00, 28.00, 'KOR', '3304.10.00.00', 'REJECTED', true, NOW(), NOW()),
(2, 'PROD-2024-003', 'Hydrating Snail Cream', 
'High-moisture cream containing 92% snail secretion filtrate. Excellent for skin regeneration and hydration, safe for sensitive skin. Gel-cream texture with non-sticky finish. Volume: 50ml, plastic container, total weight: 75g. Use after toner and serum in skincare routine.', 
32.00, 38.50, 'KOR', '3304.99.50.00', 'PENDING_REVIEW', true, NOW(), NOW()),
(2, 'PROD-2024-004', 'Waterproof Mascara Black', 
'Waterproof black mascara that maintains curling effect all day without smudging. Silicone brush allows precise application. Suitable for all eye shapes and contact lens wearers. Volume: 10ml, plastic tube, total weight: 25g. Use as final step in eye makeup routine.', 
18.00, 23.50, 'KOR', '3304.20.00.00', 'REJECTED', true, NOW(), NOW()),
(2, 'PROD-2024-005', 'Nourishing Argan Oil Shampoo', 
'Nourishing shampoo containing argan oil and keratin. Effective for damaged hair repair and shine improvement. pH 5.5 weakly acidic, suitable for all hair types. Sulfate-free formula. Volume: 500ml, pump-type plastic container, total weight: 550g. Apply to wet hair, lather, and rinse thoroughly.', 
25.00, 31.00, 'KOR', '3305.10.00.00', 'APPROVED', true, NOW(), NOW()),
(2, 'PROD-2024-006', 'Rose Garden Perfume 50ml', 
'Floral eau de parfum harmoniously blending rose and peony scents. 6-8 hours lasting power with subtle, elegant fragrance suitable for daily use. Ethanol base. Volume: 50ml, glass spray bottle, total weight: 150g. Spray lightly on neck and wrist pulse points.', 
65.00, 75.00, 'KOR', '3307.90.00.00', 'DRAFT', true, NOW(), NOW()),
(2, 'PROD-2024-007', 'Brightening Face Powder', 
'Face powder that brightens skin tone with SPF 20 sun protection. Semi-matte finish with excellent lasting power. Available in Light and Medium shades. Volume: 12g, includes compact case, total weight: 45g. Apply lightly with brush or puff after foundation.', 
28.00, 34.00, 'KOR', '3304.91.00.00', 'DRAFT', true, NOW(), NOW()),
(2, 'PROD-2024-008', 'Strengthening Hair Treatment', 
'Intensive care treatment for damaged hair with protein and amino acid complex for hair strength improvement. Recommended use 2-3 times per week. Suitable for all levels of hair damage. Volume: 200ml, tube container, total weight: 220g. Apply to wet hair after shampooing, leave for 5-10 minutes, then rinse.', 
35.00, 42.00, 'KOR', '3305.90.00.00', 'DRAFT', true, NOW(), NOW()),
(2, 'PROD-2024-009', 'Gel Nail Polish Set', 
'UV LED curing gel nail polish 5-color set with 2-week durability and chip resistance. Includes nude, red, pink, blue, and black colors. Each 10ml with brush applicator. Total weight: 200g. Apply base coat, color, and top coat, curing under UV lamp between each step.', 
40.00, 48.00, 'KOR', '3304.30.00.00', 'APPROVED', true, NOW(), NOW()),
(2, 'PROD-2024-010', 'Premium Body Lotion', 
'Premium body lotion containing shea butter and jojoba oil. Deep moisturizing and nourishing, suitable for dry skin. Vanilla and coconut fragrance with fast absorption and non-sticky formula. Volume: 300ml, pump container, total weight: 350g. Apply and massage all over body after shower.', 
30.00, 37.00, 'KOR', '3304.99.50.00', 'DRAFT', true, NOW(), NOW());

-- 4. 분석 큐(AnalysisQueue) 데이터 - 상품 등록시 자동 생성되는 분석 작업들 (수정본)
INSERT INTO analysis_queue (product_id, analysis_types, status, priority, scheduled_at, started_at, completed_at, error_message, retry_count) VALUES
(2, '["tariff_1qty", "tariff_10qty", "requirements", "precedents"]', 'COMPLETED', 1, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days', null, 0),
(1, '["tariff_1qty", "tariff_10qty", "requirements", "precedents"]', 'COMPLETED', 1, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days', null, 0),
(3, '["tariff_1qty", "tariff_10qty", "requirements", "precedents"]', 'COMPLETED', 2, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 days', null, 0),
(4, '["requirements", "precedents"]', 'FAILED', 3, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', null, 'Insufficient product information for waterproof claim analysis - missing technical specifications', 2),
(5, '["tariff_1qty", "tariff_10qty", "requirements", "precedents"]', 'COMPLETED', 1, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', null, 0),
(6, '["requirements", "precedents"]', 'FAILED', 2, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', null, 'Insufficient data for alcohol content analysis - missing detailed ingredient composition and concentration levels', 1),
(7, '["tariff_1qty", "tariff_10qty", "requirements", "precedents"]', 'PENDING', 4, NOW() + INTERVAL '5 minutes', null, null, null, 0),
(8, '["tariff_1qty", "requirements"]', 'COMPLETED', 2, NOW() - INTERVAL '1 days', NOW() - INTERVAL '1 days', NOW() - INTERVAL '20 hours', null, 0),
(8, '["tariff_10qty", "precedents"]', 'PROCESSING', 2, NOW() - INTERVAL '1 days', NOW() - INTERVAL '4 hours', null, null, 0),
(9, '["tariff_1qty", "tariff_10qty", "requirements", "precedents"]', 'COMPLETED', 1, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days', NOW() - INTERVAL '7 days', null, 0),
(10, '["tariff_1qty", "requirements"]', 'FAILED', 3, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '1 hours', null, 'Insufficient ingredient data for natural claims analysis - missing certification details and sourcing information', 1),
(10, '["tariff_10qty", "precedents"]', 'PENDING', 3, NOW() + INTERVAL '10 minutes', null, null, null, 0);


-- 5. 상품 분석 캐시(ProductAnalysisCache) 데이터 - AnalysisQueue 완료 결과에 따른 캐시
INSERT INTO product_analysis_cache (product_id, analysis_type, analysis_result, sources, confidence_score, is_valid, created_at, updated_at) VALUES
-- Product 1 (APPROVED) - 모든 분석 완료됨
(1, 'hs_code', '{"hsCode": "3304.99.50.00", "classification": "Other beauty or makeup preparations", "description": "Skincare preparations not elsewhere specified"}', '["HTSUS", "Product_Description", "Ingredient_Analysis"]', 0.95, true, NOW() - INTERVAL '6 day', NOW() - INTERVAL '6 day'),
(1, 'tariff_1qty', '{"quantity": 1, "unit_price": 45.00, "fob_price": 52.00, "tariff_rate": 0.0000, "tariff_amount": 0.00, "total_cost": 52.00}', '["KORUS_FTA", "HTSUS_3304.99.50.00"]', 0.98, true, NOW() - INTERVAL '6 day', NOW() - INTERVAL '6 day'),
(1, 'tariff_10qty', '{"quantity": 10, "unit_price": 45.00, "fob_price": 52.00, "total_value": 520.00, "tariff_amount": 0.00, "total_cost": 520.00}', '["KORUS_FTA", "Bulk_Pricing"]', 0.98, true, NOW() - INTERVAL '6 day', NOW() - INTERVAL '6 day'),
(1, 'requirements', '{"fda_registration": true, "cosmetic_facility_registration": true, "ingredient_safety": true, "labeling_compliance": true, "additional_docs": ["Ingredient_List", "Safety_Assessment"]}', '["FDA_MoCRA", "21_CFR_701", "Cosmetic_Regulations"]', 0.97, true, NOW() - INTERVAL '6 day', NOW() - INTERVAL '6 day'),
(1, 'precedents', '{"similar_products": ["Vitamin_E_Serum", "Retinol_Serum"], "approval_rate": 0.92, "common_issues": ["Ingredient_Concentration", "Stability_Testing"], "success_factors": ["Proper_Labeling", "Safety_Documentation"]}', '["Trade_History", "FDA_Database", "Import_Records"]', 0.88, true, NOW() - INTERVAL '6 day', NOW() - INTERVAL '6 day'),

-- Product 2 (APPROVED) - 모든 분석 완료됨
(2, 'hs_code', '{"hsCode": "3304.10.00.00", "classification": "Lip makeup preparations", "description": "Lipsticks, lip balms, and similar lip cosmetics"}', '["HTSUS", "Product_Category"]', 0.97, true, NOW() - INTERVAL '5 day', NOW() - INTERVAL '5 day'),
(2, 'tariff_1qty', '{"quantity": 1, "unit_price": 22.00, "fob_price": 28.00, "tariff_rate": 0.0000, "tariff_amount": 0.00, "total_cost": 28.00}', '["KORUS_FTA", "HTSUS_3304.10.00.00"]', 0.98, true, NOW() - INTERVAL '5 day', NOW() - INTERVAL '5 day'),
(2, 'tariff_10qty', '{"quantity": 10, "unit_price": 22.00, "fob_price": 28.00, "total_value": 280.00, "tariff_amount": 0.00, "total_cost": 280.00, "bulk_discount": 0.03}', '["KORUS_FTA", "Bulk_Import"]', 0.98, true, NOW() - INTERVAL '5 day', NOW() - INTERVAL '5 day'),
(2, 'requirements', '{"fda_registration": true, "color_additive_approval": true, "cosmetic_labeling": true, "safety_testing": true, "additional_docs": ["Color_Additive_Certification"]}', '["FDA_Color_Additives", "21_CFR_701", "Cosmetic_Safety"]', 0.96, true, NOW() - INTERVAL '5 day', NOW() - INTERVAL '5 day'),
(2, 'precedents', '{"similar_products": ["Matte_Lipstick", "Long_Wear_Lipstick"], "approval_rate": 0.89, "common_issues": ["Color_Additive_Approval", "Durability_Claims"], "success_factors": ["FDA_Approved_Colors", "Proper_Testing"]}', '["Cosmetic_Import_History", "Color_Database"]', 0.91, true, NOW() - INTERVAL '5 day', NOW() - INTERVAL '5 day'),

-- Product 3 (PENDING_REVIEW) - AI 분석 완료됨
(3, 'hs_code', '{"hsCode": "3304.99.50.00", "classification": "Other beauty or makeup preparations", "description": "Moisturizing and skin regeneration creams"}', '["HTSUS", "Skin_Care_Category"]', 0.94, true, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
(3, 'tariff_1qty', '{"quantity": 1, "unit_price": 32.00, "fob_price": 38.50, "tariff_rate": 0.0000, "tariff_amount": 0.00, "total_cost": 38.50}', '["KORUS_FTA", "HTSUS_3304.99.50.00"]', 0.98, true, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
(3, 'tariff_10qty', '{"quantity": 10, "unit_price": 32.00, "fob_price": 38.50, "total_value": 385.00, "tariff_amount": 0.00, "total_cost": 385.00, "bulk_discount": 0.02}', '["KORUS_FTA", "Volume_Discount"]', 0.98, true, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
(3, 'requirements', '{"fda_registration": true, "cosmetic_facility_registration": true, "snail_extract_safety": true, "sensitive_skin_testing": true, "additional_docs": ["Snail_Extract_Safety", "Dermatological_Testing"]}', '["FDA_MoCRA", "Sensitive_Skin_Guidelines"]', 0.95, true, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
(3, 'precedents', '{"similar_products": ["Snail_Essence", "Regenerating_Cream"], "approval_rate": 0.87, "common_issues": ["Extract_Concentration", "Allergy_Testing"], "success_factors": ["Proper_Concentration", "Clinical_Testing"]}', '["K_Beauty_Import_Records", "Snail_Product_Database"]', 0.89, true, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

-- Product 5 (APPROVED) - 모든 분석 완료됨
(5, 'hs_code', '{"hsCode": "3305.10.00.00", "classification": "Shampoos", "description": "Hair washing preparations in liquid, cream, or other forms"}', '["HTSUS", "Product_Function"]', 0.99, true, NOW() - INTERVAL '4 day', NOW() - INTERVAL '4 day'),
(5, 'tariff_1qty', '{"quantity": 1, "unit_price": 25.00, "fob_price": 31.00, "tariff_rate": 0.0000, "tariff_amount": 0.00, "total_cost": 31.00}', '["KORUS_FTA", "HTSUS_3305.10.00.00"]', 0.98, true, NOW() - INTERVAL '4 day', NOW() - INTERVAL '4 day'),
(5, 'tariff_10qty', '{"quantity": 10, "unit_price": 25.00, "fob_price": 31.00, "total_value": 310.00, "tariff_amount": 0.00, "total_cost": 310.00, "bulk_discount": 0.05}', '["KORUS_FTA", "Bulk_Import"]', 0.98, true, NOW() - INTERVAL '4 day', NOW() - INTERVAL '4 day'),
(5, 'requirements', '{"fda_registration": true, "cosmetic_facility_registration": true, "sulfate_free_claim": true, "ph_testing": true, "additional_docs": ["Formula_Disclosure", "pH_Test_Results"]}', '["FDA_MoCRA", "Hair_Product_Regulations"]', 0.94, true, NOW() - INTERVAL '4 day', NOW() - INTERVAL '4 day'),
(5, 'precedents', '{"similar_products": ["Organic_Shampoo", "Sulfate_Free_Shampoo"], "approval_rate": 0.94, "common_issues": ["pH_Balance", "Sulfate_Free_Claims"], "success_factors": ["Proper_Documentation", "pH_Testing"]}', '["Hair_Product_History", "Organic_Claims_DB"]', 0.93, true, NOW() - INTERVAL '4 day', NOW() - INTERVAL '4 day'),

-- Product 8 (PENDING_REVIEW) - 일부 분석만 완료됨
(8, 'hs_code', '{"hsCode": "3305.90.00.00", "classification": "Other hair preparations", "description": "Hair treatments, masks, and conditioning products"}', '["HTSUS", "Hair_Treatment_Category"]', 0.97, true, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
(8, 'tariff_1qty', '{"quantity": 1, "unit_price": 35.00, "fob_price": 42.00, "tariff_rate": 0.0000, "tariff_amount": 0.00, "total_cost": 42.00}', '["KORUS_FTA", "HTSUS_3305.90.00.00"]', 0.98, true, NOW() - INTERVAL '20 hour', NOW() - INTERVAL '20 hour'),
(8, 'requirements', '{"fda_registration": true, "protein_complex_disclosure": true, "amino_acid_documentation": true, "hair_strength_claims": true, "additional_docs": ["Protein_Analysis", "Efficacy_Studies"]}', '["FDA_Hair_Claims", "Protein_Product_Guidelines"]', 0.94, true, NOW() - INTERVAL '20 hour', NOW() - INTERVAL '20 hour'),

-- Product 8의 tariff_10qty, precedents는 아직 processing 중이므로 캐시 없음

-- Product 9 (APPROVED) - 모든 분석 완료됨
(9, 'hs_code', '{"hsCode": "3304.30.00.00", "classification": "Manicure or pedicure preparations", "description": "Nail polish, base coat, top coat preparations"}', '["HTSUS", "Product_Application"]', 0.96, true, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(9, 'tariff_1qty', '{"quantity": 1, "unit_price": 40.00, "fob_price": 48.00, "tariff_rate": 0.0000, "tariff_amount": 0.00, "total_cost": 48.00}', '["KORUS_FTA", "HTSUS_3304.30.00.00"]', 0.98, true, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(9, 'tariff_10qty', '{"quantity": 10, "unit_price": 40.00, "fob_price": 48.00, "total_value": 480.00, "tariff_amount": 0.00, "total_cost": 480.00, "set_discount": 0.08}', '["KORUS_FTA", "Nail_Set_Import"]', 0.98, true, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(9, 'requirements', '{"fda_registration": true, "cosmetic_labeling": true, "uv_safety": true, "chemical_disclosure": true, "additional_docs": ["UV_Safety_Info", "Chemical_Composition"]}', '["FDA_Cosmetics", "UV_Product_Safety"]', 0.95, true, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(9, 'precedents', '{"similar_products": ["UV_Nail_Polish", "Gel_Manicure_Set"], "approval_rate": 0.93, "common_issues": ["UV_Warnings", "Chemical_Labeling"], "success_factors": ["Proper_Warnings", "Complete_Instructions"]}', '["Nail_Product_History", "UV_Cosmetic_Database"]', 0.92, true, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');
-- 주의사항:
-- Product 4, 6, 10: 분석 실패로 캐시 데이터 없음
-- Product 7: 아직 pending 상태로 캐시 데이터 없음  
-- Product 8: tariff_10qty, precedents는 processing 중이므로 해당 캐시 없음


INSERT INTO broker_reviews (product_id, broker_id, review_status, review_comment, suggested_hs_code, requested_at, reviewed_at, created_at, updated_at) VALUES
-- Product 1 (APPROVED) - 승인, 코멘트 있음
(1, 3, 'APPROVED', 'AI 분석 결과 검증 완료. 비타민C 세럼의 HS 코드 분류와 FDA 등록 요구사항이 정확하게 적용되었습니다.', null, NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days'),

-- Product 2 (REJECTED) - HS코드 오류로 거절
(2, 3, 'REJECTED', 'HS 코드 분류 오류. 24시간 지속 매트 립스틱은 일반 립 메이크업(3304.10.00.00)이 아닌 특수 기능성 화장품(3304.99.50.00)으로 분류되어야 합니다. 지속성 클레임으로 인한 재분류 필요.', '3304.99.50.00', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days'),

-- Product 3 (PENDING_REVIEW) - 검토 중
(3, 3, 'PENDING', '달팽이 크림의 AI 분석이 완료되어 검토 중입니다. 달팽이 추출물 농도 및 민감성 피부 주장에 대한 근거 자료를 확인하고 있습니다.', null, NOW() - INTERVAL '1 day', NULL, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

-- Product 5 (APPROVED) - 승인, 코멘트 공란
(5, 3, 'APPROVED', NULL, NULL, NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days'),

-- Product 9 (APPROVED) - 승인, 코멘트 있음  
(9, 3, 'APPROVED', '젤 네일 폴리시 세트 분석 검증 완료. UV 안전 경고 및 화학 성분 공개가 적절히 문서화되었습니다.', NULL, NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days');


-- 7. 채팅 세션(ChatSession) 데이터 - 판매자/구매자 챗봇 플로우별 세션
INSERT INTO chat_sessions (user_id, session_type, language, status, session_data, created_at, updated_at) VALUES
-- Session 1: 판매자 - 기존 상품 관세 문의 (일시중지)
(2, 'SELLER_PRODUCT_INQUIRY', 'ko', 'COMPLETED', '{"flow_type": "existing_product", "product_id": 5, "product_name": "Nourishing Argan Oil Shampoo", "inquiry_type": "tariff", "quantity": 200, "step_current": "awaiting_user_response", "last_activity": "2024-09-04T12:45:00Z", "auto_pause_reason": "user_inactive_10min"}', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),
-- Session 2: 판매자 - 기존 상품 판례 문의 (활성 세션)
(2, 'SELLER_PRODUCT_INQUIRY', 'ko', 'ACTIVE', '{"flow_type": "existing_product", "product_id": 9, "product_name": "Gel Nail Polish Set", "inquiry_type": "precedents", "step_current": "awaiting_additional_info", "step_completed": "initial_requirements_provided", "user_question": "거부된 이유가 뭔지 더 자세히 알고 싶어", "context": "product_rejected_status", "next_expected_input": "specific_rejection_reasons", "last_activity": "2024-09-04T15:45:00Z"}', NOW() - INTERVAL '15 minutes', NOW()),
-- Session 3: 구매자 - 구매시 유의사항 문의 (완료된 세션)
(1, 'BUYER_PURCHASE_INQUIRY', 'en', 'COMPLETED', '{"flow_type": "precaution_inquiry", "product_id": 3, "product_name": "Hydrating Snail Cream", "inquiry_types": ["requirements", "precedents"], "step_completed": "comprehensive_answer_provided", "user_satisfied": true}', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'),
-- Session 4: 구매자 - 에러로 인한 실패 세션
(1, 'BUYER_PURCHASE_INQUIRY', 'en', 'FAILED', '{"flow_type": "precaution_inquiry", "product_mentioned": "Premium Body Lotion", "error_type": "product_not_found", "error_message": "Could not identify product from user input", "retry_count": 2, "failed_at": "2024-09-04T13:20:00Z"}', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour');