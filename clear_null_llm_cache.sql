-- llm_summary가 null인 이전 캐시 데이터 삭제
-- 이렇게 하면 다음 요청 시 새로 분석됩니다

DELETE FROM product_analysis_cache 
WHERE analysis_type = 'requirements' 
  AND (
    analysis_result->>'llm_summary' IS NULL 
    OR analysis_result->'llm_summary' = 'null'::jsonb
  );

-- 삭제된 행 수 확인
SELECT 
    '삭제 완료: ' || COUNT(*) || '개 캐시 제거됨' as result
FROM product_analysis_cache 
WHERE analysis_type = 'requirements';

