-- 1. 사용자(User) 데이터
INSERT INTO users (email, password, user_type, user_name, preferred_language, is_active, created_at, updated_at) VALUES
('buyer1@globalcosmetics.com', 'password123', 'BUYER', 'Emily Johnson', 'en', true, NOW(), NOW()),
('seller1@kbeauty.co.kr', 'password123', 'SELLER', '김민수', 'ko', true, NOW(), NOW()),
('broker1@tradepartners.com', 'password123', 'BROKER', '박상현', 'ko', true, NOW(), NOW());

-- 2. HS 코드(HsCode) 데이터
INSERT INTO hs_codes (
  hs_code, description, us_tariff_rate, requirements, 
  trade_agreements, last_updated, created_at, updated_at
) VALUES 
('3304.99.50.00', 
 'Other beauty or makeup preparations and preparations for the care of the skin (serums, creams)', 
 0.0000, 
 'FDA cosmetic registration required, ingredient safety assessment, VCRP voluntary registration, labeling compliance per 21 CFR 701', 
 '{"KORUS": "15%", "USMCA": "preferential", "GSP": "eligible"}', 
 NOW(), NOW(), NOW()),

('3307.90.00.00', 
 'Other perfumes and toilet waters', 
 0.0000, 
 'FDA cosmetic registration required, alcohol content declaration, flammable liquid shipping requirements', 
 '{"KORUS": "duty-free", "USMCA": "preferential"}', 
 NOW(), NOW(), NOW()),

('2106.90.99.00', 
 'Other food preparations not elsewhere specified (ginseng extracts, dietary supplements)', 
 0.0645, 
 'FDA prior notice required, nutritional supplement facility registration, DSHEA compliance, health claims substantiation, cGMP requirements per 21 CFR 111', 
 '{"KORUS": "8.5% -> duty-free", "USMCA": "preferential", "GSP": "not eligible"}', 
 NOW(), NOW(), NOW()),

('1904.90.00.00', 
 'Prepared foods obtained by swelling or roasting cereals (instant rice, rice cakes)', 
 0.0140, 
 'FDA prior notice required, facility registration, nutritional labeling per 21 CFR 101, allergen declaration, process filing for acidified foods if applicable', 
 '{"KORUS": "1.4% -> duty-free", "USMCA": "preferential", "GSP": "eligible"}', 
 NOW(), NOW(), NOW()),

('1905.90.90.00', 
 'Other bakers wares (rice snacks, crackers, cookies)', 
 0.0140, 
 'FDA prior notice required, facility registration, nutritional labeling, allergen declaration per FALCPA, facility inspection compliance', 
 '{"KORUS": "1.4% -> duty-free", "USMCA": "preferential", "GSP": "eligible"}', 
 NOW(), NOW(), NOW()),

('1902.30.00.00', 
 'Other pasta (instant noodles, prepared noodle products)', 
 0.0645, 
 'FDA prior notice required, facility registration, nutritional labeling, allergen declaration, sodium content disclosure', 
 '{"KORUS": "6.4% -> duty-free", "USMCA": "preferential", "GSP": "eligible"}', 
 NOW(), NOW(), NOW()),

('2103.90.90.00', 
 'Other sauces and preparations therefor; mixed condiments and seasonings', 
 0.0645, 
 'FDA prior notice required, facility registration, nutritional labeling, allergen declaration, HARPC compliance if applicable, acidified food regulations', 
 '{"KORUS": "6.4% -> duty-free", "USMCA": "preferential", "GSP": "eligible"}', 
 NOW(), NOW(), NOW()),

('2005.99.10.00', 
 'Vegetables prepared or preserved otherwise than by vinegar (kimchi, fermented vegetables)', 
 0.1190, 
 'FDA prior notice required, facility registration, HARPC compliance, acidified foods process filing, nutritional labeling, refrigeration chain documentation', 
 '{"KORUS": "11.9% -> duty-free", "USMCA": "preferential", "GSP": "eligible"}', 
 NOW(), NOW(), NOW());

 -- 3. 상품(Product) 데이터 - SELLER ID는 2번 사용자(김민수)
INSERT INTO products (seller_id, product_id, product_name, description, price, fob_price, origin_country, hs_code, status, is_active, created_at, updated_at) VALUES
(2, 'PROD-2024-001', 'Premium Vitamin C Serum', 
'High-concentration Vitamin C serum effective for skin tone improvement and anti-aging. Contains 20% Vitamin C and hyaluronic acid, suitable for all skin types. Volume: 30ml, glass bottle packaging, net weight: 50g. Use after toner in morning and evening skincare routine.', 
45.00, 52.00, 'KOR', '3304.99.50.00', 'APPROVED', true, NOW(), NOW()),

(2, 'PROD-2024-002', 'Hydrating Snail Cream', 
'High-moisture cream containing 92% snail secretion filtrate. Excellent for skin regeneration and hydration, safe for sensitive skin. Gel-cream texture with non-sticky finish. Volume: 50ml, plastic container, total weight: 75g. Use after toner and serum in skincare routine.', 
32.00, 38.50, 'KOR', '3304.99.50.00', 'REJECTED', true, NOW(), NOW()),

(2, 'PROD-2024-003', 'Premium Red Ginseng Extract', 
'6-year aged Korean red ginseng concentrated extract with 80mg ginsenosides per serving. Boosts energy and immunity, supports overall health. Sugar-free formula suitable for adults. Volume: 240ml (30ml x 8 pouches), individual foil packaging, net weight: 300g. Take 1 pouch daily on empty stomach.', 
85.00, 95.00, 'KOR', '2106.90.99.00', 'APPROVED', true, NOW(), NOW()),

(2, 'PROD-2024-004', 'Instant Cooked Rice Multipack', 
'Premium Korean short-grain rice, pre-cooked and packaged for convenience. Microwave-ready in 2 minutes, maintains fresh texture and taste. No preservatives added. Volume: 210g x 12 packs, vacuum-sealed packaging, total weight: 2.8kg. Heat in microwave for 2 minutes or steam for 3 minutes.', 
24.00, 28.00, 'KOR', '1904.90.00.00', 'APPROVED', true, NOW(), NOW()),

(2, 'PROD-2024-005', 'Korean Rice Snack Assorted Pack', 
'Traditional Korean rice snacks made with organic brown rice and seasoned with natural ingredients. Crispy texture with savory taste. Contains original, seaweed, and chili flavors. Volume: 150g (50g x 3 packs), resealable packaging, total weight: 200g. Ready to eat, store in cool dry place.', 
18.50, 22.00, 'KOR', '1905.90.90.00', 'PENDING_REVIEW', true, NOW(), NOW()),

(2, 'PROD-2024-006', 'Premium Instant Ramyeon 4-Pack', 
'Korean-style instant noodles with rich beef bone broth and chewy noodles. Medium spicy level suitable for most palates. Contains dried vegetables and seasoning powder. Volume: 120g x 4 packs, individual packaging, total weight: 600g. Cook in boiling water for 4-5 minutes.', 
12.00, 15.50, 'KOR', '1902.30.00.00', 'APPROVED', true, NOW(), NOW()),

(2, 'PROD-2024-007', 'Korean BBQ Seasoning Mix', 
'Traditional Korean barbecue seasoning blend with soy sauce, garlic, and sesame. Perfect for marinating beef, pork, or chicken. No MSG added, all-natural ingredients. Volume: 200g powder form, resealable pouch, total weight: 230g. Mix with meat 30 minutes before cooking for best flavor.', 
15.00, 18.50, 'KOR', '2103.90.90.00', 'DRAFT', true, NOW(), NOW()),

(2, 'PROD-2024-008', 'Fermented Cabbage Kimchi', 
'Authentic Korean napa cabbage kimchi fermented with traditional recipe. Probiotic-rich with balanced spicy and sour taste. Made with Korean chili powder and fish sauce. Volume: 500g glass jar, refrigerated storage required, total weight: 700g. Consume within 2 weeks of opening, store refrigerated.', 
22.00, 26.00, 'KOR', '2005.99.10.00', 'APPROVED', true, NOW(), NOW()),

(2, 'PROD-2024-009', 'Spicy Radish Kimchi (Kkakdugi)', 
'Traditional Korean cubed radish kimchi with authentic fermented taste. Crunchy texture with medium spiciness level. Rich in probiotics and vitamins. Volume: 400g plastic container, refrigerated storage required, total weight: 500g. Best consumed within 10 days of opening, keep refrigerated.', 
18.00, 21.50, 'KOR', '2005.99.10.00', 'APPROVED', true, NOW(), NOW()),

(2, 'PROD-2024-010', 'Rose Garden Perfume 50ml', 
'Floral eau de parfum featuring complex olfactory composition with top notes of Bulgarian rose petals and French peony, middle notes of jasmine sambac and lily-of-the-valley, base notes of white musk and sandalwood. Fragrance concentration: 15% aromatic compounds in ethyl alcohol (75% vol.) and purified water base. Primary aromatic molecules: phenylethyl alcohol (rose), linalool (floral), geraniol (rose-geranium), citronellol (citrus-rose), benzyl alcohol (sweet floral). Manufacturing process involves CO2 extraction and steam distillation of natural botanicals combined with synthetic aromatic molecules for consistency and longevity. Longevity: 6-8 hours on skin with moderate to strong projection (arm''s length) for first 2 hours. pH balanced at 6.5-7.0 for skin compatibility. Volume: 50ml in UV-protective cobalt blue glass bottle with precision spray atomizer, total weight: 150g. Application: 2-3 sprays on pulse points (neck, wrists) from 15cm distance. Not recommended for sensitive skin or pregnant women. Contains potential allergens: linalool, geraniol, citronellol, benzyl alcohol as per EU regulations.', 
65.00, 75.00, 'KOR', '3307.90.00.00', 'DRAFT', true, NOW(), NOW());

-- 4. 분석 큐(AnalysisQueue) 데이터 - 새로운 상품 데이터 기준으로 생성되는 분석 작업들
INSERT INTO analysis_queue (product_id, analysis_types, status, priority, scheduled_at, started_at, completed_at, error_message, retry_count) VALUES 
(1, '["tariff_1qty", "tariff_10qty", "requirements", "precedents"]', 'COMPLETED', 1, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days', null, 0),
(2, '["tariff_1qty", "tariff_10qty", "requirements", "precedents"]', 'COMPLETED', 2, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days', null, 0),
(3, '["tariff_1qty", "tariff_10qty", "requirements", "precedents"]', 'COMPLETED', 1, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', null, 0),
(4, '["tariff_1qty", "tariff_10qty", "requirements", "precedents"]', 'COMPLETED', 1, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days', null, 0),
(5, '["requirements", "precedents"]', 'FAILED', 3, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', null, 'Insufficient organic certification data for rice snack analysis - missing USDA organic verification documents', 2),
(6, '["tariff_1qty", "tariff_10qty", "requirements", "precedents"]', 'COMPLETED', 1, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 days', null, 0),
(7, '["requirements", "precedents"]', 'FAILED', 2, NOW() - INTERVAL '4 hours', NOW() - INTERVAL '3 hours', null, 'Insufficient data for MSG-free claim analysis - missing detailed ingredient composition and testing certificates', 1),
(8, '["tariff_1qty", "tariff_10qty", "requirements", "precedents"]', 'COMPLETED', 1, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days', NOW() - INTERVAL '7 days', null, 0),
(9, '["tariff_1qty", "requirements"]', 'COMPLETED', 2, NOW() - INTERVAL '1 days', NOW() - INTERVAL '1 days', NOW() - INTERVAL '20 hours', null, 0),
(9, '["tariff_10qty", "precedents"]', 'PROCESSING', 2, NOW() - INTERVAL '1 days', NOW() - INTERVAL '4 hours', null, null, 0),
(10, '["tariff_1qty", "tariff_10qty", "requirements", "precedents"]', 'PENDING', 4, NOW() + INTERVAL '5 minutes', null, null, null, 0),
(7, '["tariff_1qty", "tariff_10qty"]', 'PENDING', 3, NOW() + INTERVAL '15 minutes', null, null, null, 0),
(5, '["tariff_1qty", "tariff_10qty"]', 'PENDING', 3, NOW() + INTERVAL '30 minutes', null, null, null, 0);

-- 5. 상품 분석 캐시(ProductAnalysisCache) 데이터 - 실제 액션 가능한 요건 및 판례
INSERT INTO product_analysis_cache (product_id, analysis_type, analysis_result, sources, confidence_score, is_valid, created_at, updated_at) VALUES
-- PROD-2024-001 (비타민C 세럼, APPROVED) - 모든 분석 완료됨
(1, 'hs_code', '{"hsCode": "3304.99.50.00", "classification": "기타 미용 또는 메이크업 제품", "description": "안티에이징 페이셜 세럼"}'::jsonb, '["https://www.usitc.gov/tata/hts/bychapter/index.htm", "https://www.fda.gov/cosmetics/cosmetics-laws-regulations"]'::jsonb, 0.95, true, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
(1, 'tariff_1qty', '{"quantity": 1, "unit_price": 45.00, "fob_price": 52.00, "tariff_rate": 0.1500, "tariff_amount": 7.80, "total_cost": 59.80, "retaliatory_tariff": true}'::jsonb, '["https://ustr.gov/trade-agreements/retaliatory-measures", "https://www.usitc.gov/tata/hts/bychapter/index.htm"]'::jsonb, 0.98, true, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
(1, 'tariff_10qty', '{"quantity": 10, "unit_price": 45.00, "fob_price": 52.00, "total_value": 520.00, "tariff_amount": 78.00, "total_cost": 598.00, "retaliatory_tariff": true}'::jsonb, '["https://ustr.gov/trade-agreements/retaliatory-measures", "https://www.cbp.gov/trade/priority-issues/trade-agreements"]'::jsonb, 0.98, true, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
(1, 'requirements', '{"critical_actions": ["상호관세 15% 적용으로 가격 경쟁력 재검토 필요", "FDA 화장품 시설등록 필수 완료", "VCRP 자발적 등록 권장"], "required_documents": ["L-Ascorbic Acid 20% 농도 검증서 (제3자 분석기관)", "pH 3.5-4.0 안정성 테스트 결과", "피부 자극성 테스트 보고서", "제품 라벨 FDA 21 CFR 701 준수 확인서"], "compliance_steps": ["1단계: FDA 시설등록 및 확인번호 확보", "2단계: 성분 안전성 문서 준비", "3단계: 라벨링 검토 및 수정", "4단계: 상호관세 반영 가격 재설정"], "timeline": "총 4-6주 소요"}'::jsonb, '["https://www.fda.gov/cosmetics/cosmetics-laws-regulations", "https://www.ecfr.gov/current/title-21/chapter-I/subchapter-G/part-701"]'::jsonb, 0.97, true, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
(1, 'precedents', '{"success_cases": ["2024년 4월 레티놀 세럼 승인 - 농도 검증 및 안정성 테스트 완료로 통과", "2023년 12월 나이아신아마이드 세럼 승인 - pH 테스트 및 자극성 테스트로 민감성 피부 클레임 인정"], "failure_cases": ["2024년 2월 비타민C 세럼 거부 - 농도 검증 부족으로 반송", "2024년 1월 안티에이징 세럼 지연 - pH 불안정성으로 추가 테스트 요구"], "actionable_insights": ["비타민C 농도는 반드시 HPLC 분석법으로 검증", "pH 안정성은 6개월 이상 장기 테스트 필요", "안티에이징 클레임 시 임상시험 데이터 필수"], "risk_factors": ["상호관세로 인한 가격 상승 15%", "경쟁 제품 대비 가격 경쟁력 저하"]}'::jsonb, '["https://www.trade.gov/country-commercial-guides/south-korea-cosmetics", "https://www.fda.gov/cosmetics/cosmetics-science-research"]'::jsonb, 0.88, true, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),

-- PROD-2024-002 (달팽이 크림, REJECTED) - 모든 분석 완료됨
(2, 'hs_code', '{"hsCode": "3304.99.50.00", "classification": "기타 미용 또는 메이크업 제품", "description": "보습 및 피부 재생 크림"}'::jsonb, '["https://www.usitc.gov/tata/hts/bychapter/index.htm", "https://www.fda.gov/cosmetics/cosmetics-laws-regulations"]'::jsonb, 0.94, true, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(2, 'tariff_1qty', '{"quantity": 1, "unit_price": 32.00, "fob_price": 38.50, "tariff_rate": 0.0000, "tariff_amount": 0.00, "total_cost": 38.50}'::jsonb, '["https://ustr.gov/trade-agreements/free-trade-agreements/korus-fta", "https://www.usitc.gov/tata/hts/bychapter/index.htm"]'::jsonb, 0.98, true, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(2, 'tariff_10qty', '{"quantity": 10, "unit_price": 32.00, "fob_price": 38.50, "total_value": 385.00, "tariff_amount": 0.00, "total_cost": 385.00}'::jsonb, '["https://ustr.gov/trade-agreements/free-trade-agreements/korus-fta", "https://www.cbp.gov/trade/priority-issues/trade-agreements"]'::jsonb, 0.98, true, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(2, 'requirements', '{"critical_actions": ["고농도 활성 성분 안전성 검증", "상처 치유 클레임 임상 데이터 확보"], "required_documents": ["달팽이 분비 여과물 92% 농도 검증서", "나이아신아마이드 3% 안정성 테스트", "아데노신 0.04% 효능 입증 자료", "상처 치유 가속화 임상시험 결과", "콜라겐 생성 자극 메커니즘 연구 보고서", "민감성 피부 알레르기 테스트"], "compliance_steps": ["1단계: 기능성 화장품 분류 서류 재작성", "2단계: 고농도 성분 안전성 추가 검증", "3단계: 기능성 클레임 임상 데이터 확보"], "timeline": "재검토 및 추가 데이터 확보에 8-12주 소요", "broker_rejection_reason": "HS코드 분류 오류 및 기능성 클레임 근거 부족"}'::jsonb, '["https://www.fda.gov/cosmetics/cosmetics-laws-regulations", "https://www.fda.gov/cosmetics/cosmetics-science-research/safety-substantiation-cosmetic-products"]'::jsonb, 0.95, true, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(2, 'precedents', '{"success_cases": ["2024년 6월 재생 크림 승인 - 기능성 성분 농도 검증 및 임상 데이터로 재승인", "2024년 3월 달팽이 에센스 승인 - 적절한 농도 및 안전성 테스트로 통과"], "failure_cases": ["2024년 5월 고농도 세라마이드 크림 거부 - 기능성 클레임 과대 표기", "2024년 4월 줄기세포 크림 재검토 - 임상 데이터 부족으로 지연"], "actionable_insights": ["달팽이 추출물 90% 이상 시 기능성 화장품으로 분류", "상처 치유 클레임은 반드시 인체 적용 시험 필요", "나이아신아마이드 3% 이상 시 안정성 장기 테스트 필수"], "risk_factors": ["브로커 재검토로 인한 출시 지연", "추가 임상시험 비용 발생"], "recommended_action": "기능성 클레임 축소 또는 임상 데이터 확보 선택 필요"}'::jsonb, '["https://www.trade.gov/country-commercial-guides/south-korea-cosmetics", "https://www.kotra.or.kr/kbc/site/main/selectTradeKbcMain.do"]'::jsonb, 0.89, true, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),

-- PROD-2024-003 (홍삼 추출액, APPROVED) - 모든 분석 완료됨
(3, 'hs_code', '{"hsCode": "2106.90.99.00", "classification": "기타 조제 식료품", "description": "인삼 추출물 및 건강보조식품"}'::jsonb, '["https://www.usitc.gov/tata/hts/bychapter/index.htm", "https://www.fda.gov/food/dietary-supplements"]'::jsonb, 0.97, true, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(3, 'tariff_1qty', '{"quantity": 1, "unit_price": 85.00, "fob_price": 95.00, "tariff_rate": 0.0000, "tariff_amount": 0.00, "total_cost": 95.00, "korus_benefit": true}'::jsonb, '["https://ustr.gov/trade-agreements/free-trade-agreements/korus-fta", "https://www.usitc.gov/tata/hts/bychapter/index.htm"]'::jsonb, 0.98, true, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(3, 'tariff_10qty', '{"quantity": 10, "unit_price": 85.00, "fob_price": 95.00, "total_value": 950.00, "tariff_amount": 0.00, "total_cost": 950.00, "korus_benefit": true}'::jsonb, '["https://ustr.gov/trade-agreements/free-trade-agreements/korus-fta", "https://www.cbp.gov/trade/priority-issues/trade-agreements"]'::jsonb, 0.98, true, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(3, 'requirements', '{"critical_actions": ["FDA 사전통지 필수 제출 (도착 2-8시간 전)", "cGMP 시설등록번호 라벨 표기 필수", "건강 기능성 표현 FDA 승인 용어만 사용"], "required_documents": ["진세노사이드 80mg 함량 분석서 (제3자 검증기관)", "6년근 재배 인증서", "cGMP 시설 인증서", "제조공정도 및 HACCP 계획서", "건강기능성 표시 근거 자료"], "compliance_steps": ["1단계: FDA 식품시설 등록 및 SID 번호 확보", "2단계: DSHEA 준수 라벨 작성", "3단계: 사전통지 시스템 등록", "4단계: 수입 전 Prior Notice 제출"], "timeline": "총 6-8주 소요", "critical_deadline": "사전통지 미제출 시 100% 입항 거부"}'::jsonb, '["https://www.fda.gov/food/dietary-supplements", "https://www.ecfr.gov/current/title-21/chapter-I/subchapter-B/part-111"]'::jsonb, 0.96, true, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(3, 'precedents', '{"success_cases": ["2024년 5월 홍삼캡슐 승인 - 진세노사이드 함량 정확 표기 및 cGMP 인증으로 통과", "2024년 3월 인삼차 승인 - 사전통지 완료 및 적절한 건강기능성 표현으로 승인"], "failure_cases": ["2024년 4월 인삼음료 거부 - 사전통지 누락으로 자동 반송", "2024년 2월 홍삼정 지연 - 건강기능성 클레임 과대표기로 라벨 수정 요구"], "actionable_insights": ["진세노사이드 함량은 HPLC 분석법으로 검증 필수", "건강기능성 표현은 ''supports immune system'' 등 FDA 승인 용어만 사용", "사전통지는 운송수단별 최소 시간 준수 (항공 4시간, 해상 8시간)"], "risk_factors": ["사전통지 누락 시 자동 입항 거부", "건강기능성 과대 표기 시 라벨링 위반"]}'::jsonb, '["https://www.trade.gov/country-commercial-guides/south-korea-food-processing", "https://www.fda.gov/food/dietary-supplements/dietary-supplement-health-and-education-act-1994"]'::jsonb, 0.89, true, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),

-- PROD-2024-004 (즉석밥, APPROVED) - 모든 분석 완료됨
(4, 'hs_code', '{"hsCode": "1904.90.00.00", "classification": "팽화 또는 볶음 곡물 조제품", "description": "즉석 조리 쌀 제품"}'::jsonb, '["https://www.usitc.gov/tata/hts/bychapter/index.htm", "https://www.fda.gov/food/food-safety-modernization-act-fsma"]'::jsonb, 0.99, true, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(4, 'tariff_1qty', '{"quantity": 1, "unit_price": 24.00, "fob_price": 28.00, "tariff_rate": 0.0000, "tariff_amount": 0.00, "total_cost": 28.00, "korus_benefit": true}'::jsonb, '["https://ustr.gov/trade-agreements/free-trade-agreements/korus-fta", "https://www.usitc.gov/tata/hts/bychapter/index.htm"]'::jsonb, 0.98, true, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(4, 'tariff_10qty', '{"quantity": 10, "unit_price": 24.00, "fob_price": 28.00, "total_value": 280.00, "tariff_amount": 0.00, "total_cost": 280.00, "korus_benefit": true}'::jsonb, '["https://ustr.gov/trade-agreements/free-trade-agreements/korus-fta", "https://www.cbp.gov/trade/priority-issues/trade-agreements"]'::jsonb, 0.98, true, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(4, 'requirements', '{"critical_actions": ["FDA 사전통지 제출", "무글루텐 인증 확보", "영양성분 정확 표시"], "required_documents": ["영양성분 분석서 (150kcal/100g, 단백질 2.5g)", "무글루텐 인증서 (셀리악병 환자용)", "멸균공정 검증서 (121°C, 15분)", "포장재 FDA 승인서 (다층 플라스틱 트레이)", "유통기한 안정성 테스트 (12개월)"], "compliance_steps": ["1단계: FDA 식품시설 등록", "2단계: 멸균 공정 표준화 및 문서화", "3단계: 영양성분 검증 및 라벨 작성", "4단계: 사전통지 시스템 등록"], "timeline": "총 3-4주 소요", "quality_standards": "수분함량 65% 이하, 미생물 기준 적합"}'::jsonb, '["https://www.fda.gov/food/food-safety-modernization-act-fsma", "https://www.ecfr.gov/current/title-21/chapter-I/subchapter-B/part-101"]'::jsonb, 0.97, true, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(4, 'precedents', '{"success_cases": ["2024년 7월 현미밥 승인 - 무글루텐 인증 및 영양표시 정확으로 통과", "2024년 5월 컵밥 승인 - 멸균공정 우수하여 문제없이 승인"], "failure_cases": ["2024년 6월 냉동밥 지연 - 포장재 안전성 검증 부족으로 추가 테스트", "2024년 4월 오곡밥 반송 - 유통기한 설정 근거 부족"], "actionable_insights": ["멸균 공정은 121°C 15분 이상 필수", "무글루텐 표시 시 20ppm 이하 검증 필요", "수분함량 관리로 유통기한 12개월 확보 가능"], "risk_factors": ["포장재 안전성 검증 실패", "유통기한 설정 근거 부족"], "recommended_action": "포장재 FDA 승인 우선 확인"}'::jsonb, '["https://www.trade.gov/country-commercial-guides/south-korea-food-processing", "https://www.fda.gov/food/food-additives-petitions/food-additive-status-list"]'::jsonb, 0.94, true, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),

-- PROD-2024-006 (즉석라면, APPROVED) - 모든 분석 완료됨
(6, 'hs_code', '{"hsCode": "1902.30.00.00", "classification": "기타 파스타", "description": "즉석 라면 및 조리 면류"}'::jsonb, '["https://www.usitc.gov/tata/hts/bychapter/index.htm", "https://www.fda.gov/food/food-safety-modernization-act-fsma"]'::jsonb, 0.98, true, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(6, 'tariff_1qty', '{"quantity": 1, "unit_price": 12.00, "fob_price": 15.50, "tariff_rate": 0.0000, "tariff_amount": 0.00, "total_cost": 15.50, "korus_benefit": true}'::jsonb, '["https://ustr.gov/trade-agreements/free-trade-agreements/korus-fta", "https://www.usitc.gov/tata/hts/bychapter/index.htm"]'::jsonb, 0.98, true, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(6, 'tariff_10qty', '{"quantity": 10, "unit_price": 12.00, "fob_price": 15.50, "total_value": 155.00, "tariff_amount": 0.00, "total_cost": 155.00, "korus_benefit": true}'::jsonb, '["https://ustr.gov/trade-agreements/free-trade-agreements/korus-fta", "https://www.cbp.gov/trade/priority-issues/trade-agreements"]'::jsonb, 0.98, true, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(6, 'requirements', '{"critical_actions": ["소고기 추출물 성분 즉시 제거 또는 식물성 대체재로 변경 필수", "FDA 사전통지 제출", "나트륨 고함량 경고 표시"], "required_documents": ["소고기 성분 제거 확인서 또는 식물성 대체재 사용 증명서", "나트륨 1,890mg 함량 분석서", "글루텐 13.2% 알레르기 표시 확인서", "영양성분표 FDA 21 CFR 101 준수"], "compliance_steps": ["1단계: 소고기 성분 완전 제거 (CRITICAL)", "2단계: 대체 스프 개발 및 테스트", "3단계: 수정된 제품 영양분석", "4단계: 라벨 재작성 및 FDA 검토"], "timeline": "제품 수정 포함 8-12주 소요", "critical_warning": "소고기 성분 포함 시 USDA에 의해 100% 압류 및 폐기처분"}'::jsonb, '["https://www.fda.gov/food/food-safety-modernization-act-fsma", "https://www.aphis.usda.gov/aphis/ourfocus/animalhealth/animal-and-animal-product-import-information"]'::jsonb, 0.96, true, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(6, 'precedents', '{"success_cases": ["2024년 6월 김치라면 승인 - 소고기 성분 제거 후 식물성 스프로 변경하여 통과", "2024년 4월 비건라면 승인 - 처음부터 식물성 원료 사용으로 문제없이 승인"], "failure_cases": ["2024년 7월 불고기라면 전량 폐기 - 소고기 추출물 포함으로 USDA 압류", "2024년 5월 갈비탕면 반송 - 소뼈 추출물 검출로 수입 금지"], "actionable_insights": ["한국산 소고기/소뼈 추출물은 어떤 형태든 수입 절대 불가", "대체재로 버섯, 다시마, 치킨 추출물 사용 가능", "나트륨 함량 높아도 정확한 표시만 하면 문제없음"], "risk_factors": ["소고기 성분 포함 시 100% 압류", "제품 전량 폐기로 인한 막대한 손실"], "recommended_action": "즉시 제품 재개발 시작 권장"}'::jsonb, '["https://www.trade.gov/country-commercial-guides/south-korea-food-processing", "https://www.aphis.usda.gov/aphis/ourfocus/animalhealth"]'::jsonb, 0.91, true, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

-- PROD-2024-008 (배추김치, APPROVED) - 모든 분석 완료됨
(8, 'hs_code', '{"hsCode": "2005.99.10.00", "classification": "기타 조제 보존 채소", "description": "발효 채소 및 김치류"}'::jsonb, '["https://www.usitc.gov/tata/hts/bychapter/index.htm", "https://www.fda.gov/food/food-safety-modernization-act-fsma"]'::jsonb, 0.97, true, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),
(8, 'tariff_1qty', '{"quantity": 1, "unit_price": 22.00, "fob_price": 26.00, "tariff_rate": 0.0000, "tariff_amount": 0.00, "total_cost": 26.00, "korus_benefit": true}'::jsonb, '["https://ustr.gov/trade-agreements/free-trade-agreements/korus-fta", "https://www.usitc.gov/tata/hts/bychapter/index.htm"]'::jsonb, 0.98, true, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),
(8, 'tariff_10qty', '{"quantity": 10, "unit_price": 22.00, "fob_price": 26.00, "total_value": 260.00, "tariff_amount": 0.00, "total_cost": 260.00, "korus_benefit": true}'::jsonb, '["https://ustr.gov/trade-agreements/free-trade-agreements/korus-fta", "https://www.cbp.gov/trade/priority-issues/trade-agreements"]'::jsonb, 0.98, true, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),
(8, 'requirements', '{"critical_actions": ["HARPC 계획서 작성 및 승인", "냉장 유통 체계 구축", "멸치액젓 알레르기 표시"], "required_documents": ["발효공정 검증서 (pH 3.8-4.2 유지 증명)", "유산균 수 검사서 (1×10^8 CFU/g 이상)", "산성화식품 공정신고서 (21 CFR 114)", "냉장유통 온도기록지 (0-4°C 유지)", "어류 알레르기 표시 확인서"], "compliance_steps": ["1단계: HARPC 계획 수립 및 FDA 제출", "2단계: 발효 공정 표준화 및 문서화", "3단계: 냉장 물류 시스템 구축", "4단계: 라벨링 어류 알레르기 표시"], "timeline": "총 4-6주 소요", "cold_chain_requirement": "전 과정 0-4°C 냉장 유통 필수"}'::jsonb, '["https://www.fda.gov/food/food-safety-modernization-act-fsma", "https://www.ecfr.gov/current/title-21/chapter-I/subchapter-B/part-114"]'::jsonb, 0.95, true, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),
(8, 'precedents', '{"success_cases": ["2024년 8월 포기김치 승인 - HARPC 완벽 준수 및 냉장유통으로 통과", "2024년 6월 백김치 승인 - pH 관리 우수하여 문제없이 승인"], "failure_cases": ["2024년 7월 총각김치 지연 - pH 불안정으로 추가 발효공정 검증 요구", "2024년 5월 깍두기 반송 - 냉장유통 중단으로 품질 변화 발생"], "actionable_insights": ["pH는 반드시 3.8-4.2 범위 유지하여 병원균 억제", "냉장유통 중단 시 발효 진행으로 품질 변화 위험", "멸치액젓 등 어류 성분은 반드시 알레르기 표시"], "risk_factors": ["냉장유통 실패 시 품질 변화", "pH 관리 실패 시 식품안전 문제"], "recommended_action": "냉장 물류 파트너십 우선 구축"}'::jsonb, '["https://www.trade.gov/country-commercial-guides/south-korea-food-processing", "https://www.fda.gov/food/hazard-analysis-critical-control-point-haccp"]'::jsonb, 0.87, true, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),

-- PROD-2024-009 (깍두기, 일부 분석 완료됨) - tariff_1qty, requirements만 완료
(9, 'hs_code', '{"hsCode": "2005.99.10.00", "classification": "기타 조제 보존 채소", "description": "발효 무 김치"}'::jsonb, '["https://www.usitc.gov/tata/hts/bychapter/index.htm", "https://www.fda.gov/food/food-safety-modernization-act-fsma"]'::jsonb, 0.96, true, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
(9, 'tariff_1qty', '{"quantity": 1, "unit_price": 18.00, "fob_price": 21.50, "tariff_rate": 0.0000, "tariff_amount": 0.00, "total_cost": 21.50, "korus_benefit": true}'::jsonb, '["https://ustr.gov/trade-agreements/free-trade-agreements/korus-fta", "https://www.usitc.gov/tata/hts/bychapter/index.htm"]'::jsonb, 0.98, true, NOW() - INTERVAL '20 hours', NOW() - INTERVAL '20 hours'),
(9, 'requirements', '{"critical_actions": ["2cm 큐브 규격 표준화", "스코빌 지수 1,200-1,800 SHU 검증", "냉장 유통 체계 확립"], "required_documents": ["무 큐브 크기 규격서 (2cm ±0.2cm)", "매운맛 스코빌 지수 측정서", "발효온도 기록지 (4-8°C)", "미생물 검사서 (유산균 5×10^7 CFU/g 이상)", "어류 알레르기 표시 (새우젓 포함)"], "compliance_steps": ["1단계: 무 절단 규격 표준화", "2단계: 매운맛 수준 객관적 측정", "3단계: 발효 온도 관리 시스템 구축", "4단계: 품질관리 문서화"], "timeline": "부분 분석 중 - 추가 2-3주 소요 예상", "pending_analysis": "10개 수량 관세 및 판례 분석 진행 중"}'::jsonb, '["https://www.fda.gov/food/food-safety-modernization-act-fsma", "https://www.ecfr.gov/current/title-21/chapter-I/subchapter-B/part-114"]'::jsonb, 0.94, true, NOW() - INTERVAL '20 hours', NOW() - INTERVAL '20 hours');

-- 6. 브로커 리뷰(BrokerReview) 데이터 - AI 분석 완료된 제품들만 브로커 검토 요청
INSERT INTO broker_reviews (product_id, broker_id, review_status, review_comment, suggested_hs_code, requested_at, reviewed_at, created_at, updated_at) VALUES 
-- PROD-2024-001 (비타민C 세럼, APPROVED) - 승인, 코멘트 있음
(1, 3, 'APPROVED', 'AI 분석 결과 검증 완료. 20% L-Ascorbic Acid 함유 비타민C 세럼의 HS 코드 분류와 FDA 화장품 등록 요구사항이 정확하게 적용되었습니다. pH 3.5-4.0 범위의 안정성 자료도 적절히 문서화되었습니다.', null, NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days'),

-- PROD-2024-002 (달팽이 크림, REJECTED) - HS코드 분류 오류로 거절
(2, 3, 'REJECTED', 'HS 코드 분류 오류. 달팽이 분비 여과물 92% 함유 및 Niacinamide 3%, Adenosine 0.04% 등 고농도 기능성 성분을 포함한 겔-크림 하이브리드 제품은 단순 보습 크림이 아닌 특수 기능성 스킨케어 제품으로 분류되어야 합니다. 상처 치유 가속화 및 콜라겐 생성 자극 기능으로 인해 재분류 필요.', '3304.99.50.00', NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days'),

-- PROD-2024-003 (홍삼 추출액, APPROVED) - 승인, 코멘트 있음
(3, 3, 'APPROVED', '6년근 홍삼 농축액의 AI 분석 검증 완료. 진세노사이드 80mg 함량 및 DSHEA 준수 요구사항이 정확히 반영되었습니다. 건강보조식품으로서의 cGMP 시설 등록과 건강 기능성 표시 기준도 적절합니다.', null, NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days'),

-- PROD-2024-004 (즉석밥, APPROVED) - 승인, 코멘트 공란
(4, 3, 'APPROVED', null, null, NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days'),

-- PROD-2024-006 (즉석라면, APPROVED) - 승인, 코멘트 있음
(6, 3, 'APPROVED', '프리미엄 즉석라면 4팩의 분석 검증 완료. 글루텐 13.2% 함량, 나트륨 1,890mg 표시 및 알레르기 유발 요소(글루텐, 대두, 갑각류) 표시가 정확히 문서화되었습니다. FDA 사전통지 및 영양표시 요구사항 충족 확인.', null, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day'),

-- PROD-2024-008 (배추김치, APPROVED) - 승인, 코멘트 있음  
(8, 3, 'APPROVED', '전통 배추김치의 AI 분석 검증 완료. pH 3.8-4.2 범위의 발효 관리, 유산균 수 1×10^8 CFU/g 및 HARPC 준수 요구사항이 적절히 반영되었습니다. 어류 성분(멸치액젓) 알레르기 표시도 정확합니다.', null, NOW() - INTERVAL '8 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '8 days', NOW() - INTERVAL '7 days'),

-- PROD-2024-009 (깍두기, PENDING) - 검토 중 (일부 분석만 완료됨)
(9, 3, 'PENDING', null, null, NOW() - INTERVAL '1 day', null, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');

-- 7. 채팅 세션(ChatSession) 데이터 - 판매자/구매자 챗봇 플로우별 세션
INSERT INTO chat_sessions (user_id, session_type, language, status, session_data, created_at, updated_at) VALUES 
-- Session 1: 판매자 - 기존 상품 관세 문의 (완료)
(2, 'SELLER_PRODUCT_INQUIRY', 'ko', 'COMPLETED', '{"flow_type": "HS_CODE", "product_id": "PROD-2024-003", "product_name": "Premium Red Ginseng Extract", "inquiry_type": "tariff", "quantity": 50, "step_current": "awaiting_user_response", "last_activity": "2024-09-04T12:45:00Z"}', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '3 hours'),

-- Session 2: 판매자 - 기존 상품 판례 문의 (활성 세션)
(2, 'SELLER_PRODUCT_INQUIRY', 'ko', 'ACTIVE', '{"flow_type": "PRECEDENTS", "product_id": "PROD-2024-006", "product_name": "Premium Instant Ramyeon 4-Pack", "inquiry_type": "precedents", "step_current": "awaiting_additional_info", "step_completed": "initial_requirements_provided", "user_question": "나트륨 함량이 높은데 수입 시 문제 될까요?", "context": "sodium_content_concern", "next_expected_input": "nutritional_labeling_requirements", "last_activity": "2024-09-04T15:45:00Z"}', NOW() - INTERVAL '15 minutes', NOW()),

-- Session 3: 구매자 - 구매시 요건 문의 (완료된 세션)
(1, 'BUYER_PURCHASE_INQUIRY', 'en', 'COMPLETED', '{"flow_type": "REQUIREMENTS", "product_id": "PROD-2024-002", "product_name": "Hydrating Snail Cream", "inquiry_types": ["requirements", "precedents"], "step_completed": "comprehensive_answer_provided", "user_satisfied": true}', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'),

-- Session 4: 구매자 - 에러로 인한 실패 세션
(1, 'BUYER_PURCHASE_INQUIRY', 'en', 'FAILED', '{"flow_type": null, "product_mentioned": "Premium Body Lotion", "error_type": "product_not_found", "error_message": "Could not identify product from user input - product not in catalog", "retry_count": 2, "failed_at": "2024-09-04T13:20:00Z"}', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour');

-- 8. 채팅 메시지(ChatMessage) 데이터 - 판매자/구매자 챗봇 로직 반영
INSERT INTO chat_messages (session_id, sender_type, message_content, message_type, metadata, created_at) VALUES
-- Session 1: 판매자 챗봇 - 기존 상품 관세 문의 플로우 (비타민C 세럼 - 상호관세 15%)
(1, 'AI', '안녕하세요! 어떤 도움이 필요하신가요?', 'TEXT',
 '{"language": "ko", "step": "main_menu", "user_type": "seller"}', NOW() - INTERVAL '3 hours'),

(1, 'USER', '기존 상품 문의', 'BUTTON',
 '{"language": "ko", "selected": "existing_product", "user_type": "seller"}', NOW() - INTERVAL '3 hours'),

(1, 'AI', '상품명을 입력해주세요.', 'TEXT',
 '{"language": "ko", "step": "product_identification", "input_type": "text"}', NOW() - INTERVAL '3 hours'),

(1, 'USER', 'Premium Vitamin C Serum', 'TEXT',
 '{"language": "ko", "step": "product_name_input", "product_name": "Premium Vitamin C Serum"}', NOW() - INTERVAL '3 hours'),

(1, 'AI', 'Premium Vitamin C Serum이 맞나요? 20% L-Ascorbic Acid 함유 안티에이징 세럼이군요.', 'TEXT',
 '{"language": "ko", "step": "product_confirmation", "product_id": "PROD-2024-001", "product_name": "Premium Vitamin C Serum"}', NOW() - INTERVAL '3 hours'),

(1, 'USER', '응, 맞아', 'BUTTON',
 '{"language": "ko", "confirmed": true, "product_id": "PROD-2024-001"}', NOW() - INTERVAL '3 hours'),

(1, 'AI', '어떤 정보가 필요하신가요?', 'BUTTON_GROUP',
 '{"language": "ko", "step": "query_type_selection", "options": ["HS코드", "관세", "요건", "판례"], "product_id": "PROD-2024-001"}', NOW() - INTERVAL '3 hours'),

(1, 'USER', '관세', 'BUTTON',
 '{"language": "ko", "selected": "tariff", "product_id": "PROD-2024-001"}', NOW() - INTERVAL '3 hours'),

(1, 'AI', '구매 개수를 입력해주세요.', 'TEXT',
 '{"language": "ko", "step": "quantity_input", "input_type": "number"}', NOW() - INTERVAL '3 hours'),

(1, 'USER', '50', 'TEXT',
 '{"language": "ko", "quantity": 50, "product_id": "PROD-2024-001"}', NOW() - INTERVAL '3 hours'),

(1, 'AI', '비타민C 세럼(HS 3304.99.50.00)은 현재 상호관세로 15%가 부과됩니다. 50개 수출시 총 FOB 가격 $2,600, 관세 $390이 추가되어 총 수입비용은 $2,990입니다. 화장품이므로 FDA 등록, 성분 안전성 평가, 21 CFR 701 라벨링 준수가 필요합니다.', 'TEXT',
 '{"language": "ko", "sources": ["US_RETALIATORY_TARIFF", "HTSUS_3304.99.50.00"], "confidence": 0.98, "product_inquiry_id": 1, "tariff_applied": 390.0, "tariff_rate": 0.15}', NOW() - INTERVAL '3 hours'),

(1, 'AI', '다른 정보가 더 필요하신가요?', 'BUTTON_GROUP',
 '{"language": "ko", "step": "additional_actions", "options": ["HS코드", "요건", "판례", "처음으로"], "product_id": "PROD-2024-001"}', NOW() - INTERVAL '3 hours'),

-- Session 2: 판매자 챗봇 - 기존 상품 판례 문의 플로우 (즉석라면 - 소고기 스프 문제)
(2, 'AI', '안녕하세요! 어떤 도움이 필요하신가요?', 'TEXT', '{"language": "ko", "step": "main_menu", "user_type": "seller"}', NOW() - INTERVAL '2 hours'),
(2, 'USER', '기존 상품 문의', 'BUTTON', '{"language": "ko", "selected": "existing_product", "user_type": "seller"}', NOW() - INTERVAL '2 hours'),
(2, 'AI', '상품명을 입력해주세요.', 'TEXT', '{"language": "ko", "step": "product_identification", "input_type": "text"}', NOW() - INTERVAL '2 hours'),
(2, 'USER', 'Premium Instant Ramyeon 4-Pack', 'TEXT', '{"language": "ko", "step": "product_name_input", "product_name": "Premium Instant Ramyeon 4-Pack"}', NOW() - INTERVAL '2 hours'),
(2, 'AI', 'Premium Instant Ramyeon 4-Pack이 맞나요?', 'TEXT', '{"language": "ko", "step": "product_confirmation", "product_id": "PROD-2024-006", "product_name": "Premium Instant Ramyeon 4-Pack"}', NOW() - INTERVAL '2 hours'),
(2, 'USER', '응, 맞아', 'BUTTON', '{"language": "ko", "confirmed": true, "product_id": "PROD-2024-006"}', NOW() - INTERVAL '2 hours'),
(2, 'AI', '어떤 정보가 필요하신가요?', 'BUTTON_GROUP', '{"language": "ko", "step": "query_type_selection", "options": ["HS코드", "관세", "요건", "판례"], "product_id": "PROD-2024-006"}', NOW() - INTERVAL '2 hours'),
(2, 'USER', '판례', 'BUTTON', '{"language": "ko", "selected": "precedents", "product_id": "PROD-2024-006"}', NOW() - INTERVAL '2 hours'),
(2, 'AI', '이 라면에 소고기 추출물이 포함된 스프가 있다면, 미국은 한국산 소고기 제품 수입을 금지하고 있어, 소고기 스프가 포함된 라면은 100% 적발 시 폐기처분됩니다. 유사 제품들이 모두 통관 거부되었습니다.', 'TEXT', '{"language": "ko", "sources": ["USDA_BEEF_BAN", "CBP_Seizure_Records"], "confidence": 0.99, "product_inquiry_id": 2, "alert_level": "critical"}', NOW() - INTERVAL '2 hours'),
(2, 'USER', '소고기 스프 때문에 문제될까요?', 'TEXT', '{"language": "ko", "additional_question": "beef_soup_concern", "product_id": "PROD-2024-006"}', NOW() - INTERVAL '15 minutes'),
(2, 'AI', '네, USDA 규정상 한국산 소고기 및 소고기 추출물은 수입 금지 품목입니다. 8시간 우린 소뼈 추출물이 포함된 스프는 반드시 적발되며, FDA와 USDA가 공동으로 100% 검역합니다. 대안으로 소고기 성분을 제거한 버전 또는 식물성 대체재로 제품 수정이 필요합니다.', 'TEXT', '{"language": "ko", "step": "beef_restriction_details", "sources": ["USDA_Import_Ban", "9_CFR_94"], "confidence": 0.99, "recommendation": "product_reformulation"}', NOW() - INTERVAL '15 minutes'),

-- Session 3: 구매자 챗봇 - 구매시 요 문의 플로우 (홍삼 추출액 - FDA Prior Notice)
(3, 'AI', 'Hello! How can I help you with your food import inquiries today?', 'TEXT', '{"language": "en", "step": "greeting", "user_type": "buyer"}', NOW() - INTERVAL '1 hour'),
(3, 'USER', 'I want to know about import requirements for Premium Red Ginseng Extract from Korea', 'TEXT', '{"language": "en", "intent": "requirements_inquiry", "product_mentioned": "Premium Red Ginseng Extract"}', NOW() - INTERVAL '1 hour'),
(3, 'AI', 'I found the Premium Red Ginseng Extract in our catalog. This is a 6-year aged Korean red ginseng concentrated extract with 80mg ginsenosides per serving. As a dietary supplement, it has specific FDA requirements. Would you like to know the detailed import requirements?', 'TEXT', '{"language": "en", "step": "product_identification", "product_id": "PROD-2024-003", "confirmation_required": true}', NOW() - INTERVAL '1 hour'),
(3, 'USER', 'Yes, please tell me all the requirements, especially about FDA notices', 'TEXT', '{"language": "en", "confirmed": true, "product_id": "PROD-2024-003", "inquiry_type": "requirements_and_prior_notice"}', NOW() - INTERVAL '1 hour'),
(3, 'AI', E'For importing Premium Red Ginseng Extract to the US, you need:\n\n**Critical Requirement - FDA Prior Notice:**\nSince May 6, 2009, FDA requires Prior Notice for ALL food imports (including dietary supplements) unless specifically exempted. You must submit Prior Notice before the shipment arrives.\n\n**Exemptions:** Only personal use (not for sale/distribution) to yourself, friends, or family.\n\n**Other Requirements:**\n1) FDA facility registration for manufacturer\n2) DSHEA compliance for dietary supplements\n3) cGMP requirements (21 CFR 111)\n4) Nutritional supplement labeling\n5) Health claims substantiation\n6) Ginsenoside content verification\n\n**Important:** Commercial imports without Prior Notice will be refused entry.', 'TEXT', '{"language": "en", "sources": ["FDA_Prior_Notice_Rule", "21_CFR_1.276", "DSHEA_Requirements"], "confidence": 0.99, "product_inquiry_id": 3}', NOW() - INTERVAL '1 hour'),
(3, 'USER', 'What happens if I forget to submit Prior Notice?', 'TEXT', '{"language": "en", "additional_request": "prior_notice_consequences", "product_id": "PROD-2024-003"}', NOW() - INTERVAL '1 hour'),
(3, 'AI', E'If you forget Prior Notice, the consequences are severe:\n\n• **Automatic refusal of entry** - no exceptions\n• **Detention at port** until proper notice is filed\n• **Storage fees** accumulate daily\n• **Potential product spoilage** during detention\n• **Re-export or destruction** if notice issues cannot be resolved\n\n**Timeline:** Prior Notice must be submitted no more than 15 days before arrival and no less than:\n- 2 hours for road transport\n- 4 hours for air/rail transport  \n- 8 hours for water transport\n\nThe FDA system will not allow entry without valid Prior Notice confirmation number.', 'TEXT', '{"language": "en", "sources": ["FDA_Import_Procedures", "CBP_Entry_Requirements"], "confidence": 0.98, "additional_info": true}', NOW() - INTERVAL '1 hour'),
(3, 'USER', 'Thank you, this is very helpful!', 'TEXT', '{"language": "en", "intent": "gratitude"}', NOW() - INTERVAL '1 hour'),
(3, 'AI', 'You are welcome! Remember, Prior Notice is mandatory for all commercial food imports. I recommend setting up your FDA facility registration first, then submitting Prior Notice within the required timeframe for each shipment.', 'TEXT', '{"language": "en", "step": "closing", "offer_additional_help": true}', NOW() - INTERVAL '1 hour'),

-- Session 4: 구매자 - 에러로 인한 실패 세션 (상품 미등록)
(4, 'AI', 'Hello! How can I help you with your cosmetics import inquiries today?', 'TEXT', '{"language": "en", "step": "greeting", "user_type": "buyer"}', NOW() - INTERVAL '1 hour'),
(4, 'USER', 'Hi, I want to know about importing Premium Body Lotion from Korea', 'TEXT', '{"language": "en", "intent": "product_inquiry", "product_mentioned": "Premium Body Lotion"}', NOW() - INTERVAL '1 hour'),
(4, 'AI', 'I am searching for Premium Body Lotion in our product catalog...', 'TEXT', '{"language": "en", "step": "product_search", "search_query": "Premium Body Lotion"}', NOW() - INTERVAL '1 hour'),
(4, 'AI', 'I could not find "Premium Body Lotion" in our current product catalog. Could you provide more details about the product, such as the manufacturer name, specific ingredients, or product code?', 'TEXT', '{"language": "en", "step": "clarification_request", "error_type": "product_not_found", "retry_attempt": 1}', NOW() - INTERVAL '1 hour'),
(4, 'USER', 'It contains shea butter and jojoba oil, made by Korean cosmetics company', 'TEXT', '{"language": "en", "additional_details": "shea butter, jojoba oil, Korean manufacturer"}', NOW() - INTERVAL '1 hour'),
(4, 'AI', E'Thank you for the additional details. I searched for Korean body lotions with shea butter and jojoba oil, but still cannot find this specific product in our registered catalog. This may be because:\n\n1) The product is not yet registered with our platform\n2) It may be listed under a different name\n3) The manufacturer has not submitted this product for analysis\n\nWould you like me to provide general import guidelines for Korean body lotions, or would you prefer to contact the manufacturer to register the product first?', 'TEXT', '{"language": "en", "step": "product_not_found_final", "error_type": "product_not_in_catalog", "suggested_actions": ["general_guidelines", "manufacturer_registration"], "failed_at": "2024-09-04T13:20:00Z"}', NOW() - INTERVAL '1 hour');

-- 9. 상품 문의(ProductInquiry) 데이터 - 챗봇 플로우에 따른 구조화된 문의
INSERT INTO product_inquiries (user_id, product_id, inquiry_type, inquiry_data, ai_response, response_sources, from_cache, response_time_ms, created_at, updated_at) VALUES 
-- 판매자 관세 문의 (Session 1에서 생성 - 비타민C 세럼, 상호관세 15%)
(2, 1, 'TARIFF', 
'{"product": "Premium Vitamin C Serum", "unit_price": 45.00, "fob_price": 52.00, "quantity": 50, "destination": "US", "origin": "KOR", "hs_code": "3304.99.50.00", "user_flow": "existing_product_inquiry"}', 
'비타민C 세럼(HS 3304.99.50.00)은 현재 상호관세로 15%가 부과됩니다. 50개 수출시 총 FOB 가격 $2,600, 관세 $390이 추가되어 총 수입비용은 $2,990입니다. 화장품이므로 FDA 등록, 성분 안전성 평가, 21 CFR 701 라벨링 준수가 필요합니다.', 
'["US_RETALIATORY_TARIFF", "HTSUS_3304.99.50.00", "FDA_Cosmetic_Regulations"]', 
false, 1650, NOW() - INTERVAL '3 hours', NOW() - INTERVAL '3 hours'),

-- 판매자 판례 문의 (Session 2에서 생성 - 즉석라면, 소고기 스프 문제)
(2, 6, 'PRECEDENTS', 
'{"product": "Premium Instant Ramyeon 4-Pack", "ingredients": "beef bone extract soup", "processing": "8-hour simmering", "market": "US", "concern": "beef_import_ban", "user_flow": "existing_product_inquiry"}', 
'이 라면에 소고기 추출물이 포함된 스프가 있다면, 미국은 한국산 소고기 제품 수입을 금지하고 있어, 소고기 스프가 포함된 라면은 100% 적발 시 폐기처분됩니다. 유사 제품들이 모두 통관 거부되었습니다. USDA 규정상 한국산 소고기 및 소고기 추출물은 수입 금지 품목입니다. 대안으로 소고기 성분을 제거한 버전 또는 식물성 대체재로 제품 수정이 필요합니다.', 
'["USDA_BEEF_BAN", "CBP_Seizure_Records", "9_CFR_94", "USDA_Import_Ban"]', 
false, 2280, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),

-- 구매자 요건 문의 (Session 3에서 생성 - 홍삼 추출액, FDA Prior Notice)
(1, 3, 'REQUIREMENTS', 
'{"product": "Premium Red Ginseng Extract", "type": "dietary_supplement", "ginsenosides": "80mg per serving", "market": "US", "concern": "prior_notice_requirements", "user_flow": "buyer_precaution_inquiry"}', 
'For importing Premium Red Ginseng Extract to the US, you need: **Critical Requirement - FDA Prior Notice:** Since May 6, 2009, FDA requires Prior Notice for ALL food imports (including dietary supplements) unless specifically exempted. You must submit Prior Notice before the shipment arrives. **Exemptions:** Only personal use (not for sale/distribution) to yourself, friends, or family. **Other Requirements:** 1) FDA facility registration for manufacturer, 2) DSHEA compliance for dietary supplements, 3) cGMP requirements (21 CFR 111), 4) Nutritional supplement labeling, 5) Health claims substantiation, 6) Ginsenoside content verification. **Important:** Commercial imports without Prior Notice will be refused entry.', 
'["FDA_Prior_Notice_Rule", "21_CFR_1.276", "DSHEA_Requirements", "FDA_Import_Procedures"]', 
false, 2450, NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour');

-- 10. 관세 계산(TariffCalculation) 데이터 - ProductAnalysisCache 기반 계산
INSERT INTO tariff_calculations (product_id, buyer_id, quantity, total_value, tariff_rate, tariff_amount, total_with_tariff, calculation_sources, calculated_at) VALUES 
-- PROD-2024-001 (Premium Vitamin C Serum) - APPROVED, 상호관세 15%
(1, 1, 1, 52.00, 0.1500, 7.80, 59.80, '["US_RETALIATORY_TARIFF", "HTSUS_3304.99.50.00", "BrokerReview_Approved"]', NOW() - INTERVAL '6 days'),
(1, 1, 10, 520.00, 0.1500, 78.00, 598.00, '["US_RETALIATORY_TARIFF", "HTSUS_3304.99.50.00", "BrokerReview_Approved"]', NOW() - INTERVAL '6 days'),
-- ChatMessage Session 1에서 판매자가 문의한 비타민C 세럼 관세 (50개)
(1, 1, 50, 2600.00, 0.1500, 390.00, 2990.00, '["US_RETALIATORY_TARIFF", "HTSUS_3304.99.50.00", "BrokerReview_Approved"]', NOW() - INTERVAL '3 hours'),

-- PROD-2024-002 (Hydrating Snail Cream) - REJECTED (HS코드 수정 후 재계산)
(2, 1, 1, 38.50, 0.0000, 0.00, 38.50, '["KORUS_FTA", "HTSUS_3304.99.50.00", "BrokerReview_Corrected_HSCode"]', NOW() - INTERVAL '7 days'),
(2, 1, 10, 385.00, 0.0000, 0.00, 385.00, '["KORUS_FTA", "HTSUS_3304.99.50.00", "BrokerReview_Corrected_HSCode"]', NOW() - INTERVAL '7 days'),

-- PROD-2024-003 (Premium Red Ginseng Extract) - APPROVED
(3, 1, 1, 95.00, 0.0000, 0.00, 95.00, '["KORUS_FTA", "HTSUS_2106.90.99.00", "BrokerReview_Approved"]', NOW() - INTERVAL '5 days'),
(3, 1, 10, 950.00, 0.0000, 0.00, 950.00, '["KORUS_FTA", "HTSUS_2106.90.99.00", "BrokerReview_Approved"]', NOW() - INTERVAL '5 days'),

-- PROD-2024-004 (Instant Cooked Rice Multipack) - APPROVED
(4, 1, 1, 28.00, 0.0000, 0.00, 28.00, '["KORUS_FTA", "HTSUS_1904.90.00.00", "BrokerReview_Approved"]', NOW() - INTERVAL '4 days'),
(4, 1, 10, 280.00, 0.0000, 0.00, 280.00, '["KORUS_FTA", "HTSUS_1904.90.00.00", "BrokerReview_Approved"]', NOW() - INTERVAL '4 days'),

-- PROD-2024-006 (Premium Instant Ramyeon 4-Pack) - APPROVED
(6, 1, 1, 15.50, 0.0000, 0.00, 15.50, '["KORUS_FTA", "HTSUS_1902.30.00.00", "BrokerReview_Approved"]', NOW() - INTERVAL '2 days'),
(6, 1, 10, 155.00, 0.0000, 0.00, 155.00, '["KORUS_FTA", "HTSUS_1902.30.00.00", "BrokerReview_Approved"]', NOW() - INTERVAL '2 days'),

-- PROD-2024-008 (Fermented Cabbage Kimchi) - APPROVED
(8, 1, 1, 26.00, 0.0000, 0.00, 26.00, '["KORUS_FTA", "HTSUS_2005.99.10.00", "BrokerReview_Approved"]', NOW() - INTERVAL '8 days'),
(8, 1, 10, 260.00, 0.0000, 0.00, 260.00, '["KORUS_FTA", "HTSUS_2005.99.10.00", "BrokerReview_Approved"]', NOW() - INTERVAL '8 days'),

-- PROD-2024-009 (Spicy Radish Kimchi) - tariff_1qty만 분석 완료
(9, 1, 1, 21.50, 0.0000, 0.00, 21.50, '["KORUS_FTA", "HTSUS_2005.99.10.00", "ProductAnalysisCache", "Partial_Analysis"]', NOW() - INTERVAL '20 hours');
