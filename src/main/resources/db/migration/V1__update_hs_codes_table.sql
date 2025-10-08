-- HS 코드 테이블 수정
-- requirements와 trade_agreements 컬럼 제거, reasoning 컬럼 추가

-- 기존 컬럼 제거
ALTER TABLE hs_codes DROP COLUMN IF EXISTS requirements;
ALTER TABLE hs_codes DROP COLUMN IF EXISTS trade_agreements;

-- reasoning 컬럼 추가
ALTER TABLE hs_codes ADD COLUMN IF NOT EXISTS reasoning TEXT;

-- 인덱스 추가 (성능 향상)
CREATE INDEX IF NOT EXISTS idx_hs_codes_hs_code ON hs_codes(hs_code);
CREATE INDEX IF NOT EXISTS idx_hs_codes_last_updated ON hs_codes(last_updated);

-- 코멘트 추가
COMMENT ON COLUMN hs_codes.description IS 'HS 코드 설명 (AI 분석 결과의 combined_description)';
COMMENT ON COLUMN hs_codes.us_tariff_rate IS '미국 관세율 (AI 분석 결과)';
COMMENT ON COLUMN hs_codes.reasoning IS 'AI 분석 근거 및 관세 관련 설명';
