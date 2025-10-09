package com.suracle.backend_api.service.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 상품 키워드를 화학 정식명/CAS로 매핑하는 유틸리티
 * EPA CompTox 등 성분 기반 검색 시 사용
 */
public class ChemicalNameMapper {
    
    private static final Map<String, ChemicalInfo> INGREDIENT_MAP = new HashMap<>();
    
    static {
        // 비타민류
        INGREDIENT_MAP.put("vitamin c", new ChemicalInfo("ascorbic acid", "50-81-7"));
        INGREDIENT_MAP.put("vitamin e", new ChemicalInfo("tocopherol", "59-02-9"));
        INGREDIENT_MAP.put("vitamin a", new ChemicalInfo("retinol", "68-26-8"));
        INGREDIENT_MAP.put("vitamin b3", new ChemicalInfo("niacinamide", "98-92-0"));
        INGREDIENT_MAP.put("niacinamide", new ChemicalInfo("nicotinamide", "98-92-0"));
        
        // 화장품 성분
        INGREDIENT_MAP.put("hyaluronic acid", new ChemicalInfo("hyaluronan", "9004-61-9"));
        INGREDIENT_MAP.put("retinol", new ChemicalInfo("retinol", "68-26-8"));
        INGREDIENT_MAP.put("salicylic acid", new ChemicalInfo("salicylic acid", "69-72-7"));
        INGREDIENT_MAP.put("glycolic acid", new ChemicalInfo("glycolic acid", "79-14-1"));
        INGREDIENT_MAP.put("lactic acid", new ChemicalInfo("lactic acid", "50-21-5"));
        
        // 보존제/첨가물
        INGREDIENT_MAP.put("parabens", new ChemicalInfo("methylparaben", "99-76-3"));
        INGREDIENT_MAP.put("formaldehyde", new ChemicalInfo("formaldehyde", "50-00-0"));
        INGREDIENT_MAP.put("phthalates", new ChemicalInfo("diethyl phthalate", "84-66-2"));
        
        // 식품 성분
        INGREDIENT_MAP.put("caffeine", new ChemicalInfo("caffeine", "58-08-2"));
        INGREDIENT_MAP.put("citric acid", new ChemicalInfo("citric acid", "77-92-9"));
        INGREDIENT_MAP.put("sodium benzoate", new ChemicalInfo("sodium benzoate", "532-32-1"));
    }
    
    /**
     * 상품 키워드를 화학 정식명으로 변환
     * @param keyword 상품 키워드 (예: "vitamin c")
     * @return 화학 정식명 (예: "ascorbic acid"), 매핑 없으면 원본 반환
     */
    public static String toChemicalName(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return keyword;
        }
        
        String normalized = keyword.toLowerCase().trim();
        ChemicalInfo info = INGREDIENT_MAP.get(normalized);
        
        return info != null ? info.getChemicalName() : keyword;
    }
    
    /**
     * 상품 키워드를 CAS 번호로 변환
     * @param keyword 상품 키워드
     * @return CAS 번호, 매핑 없으면 null
     */
    public static String toCasNumber(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        
        String normalized = keyword.toLowerCase().trim();
        ChemicalInfo info = INGREDIENT_MAP.get(normalized);
        
        return info != null ? info.getCasNumber() : null;
    }
    
    /**
     * 화학 정식명과 CAS를 함께 반환
     */
    public static ChemicalInfo getChemicalInfo(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new ChemicalInfo(keyword, null);
        }
        
        String normalized = keyword.toLowerCase().trim();
        ChemicalInfo info = INGREDIENT_MAP.get(normalized);
        
        return info != null ? info : new ChemicalInfo(keyword, null);
    }
    
    /**
     * 화학물질 정보 클래스
     */
    public static class ChemicalInfo {
        private final String chemicalName;
        private final String casNumber;
        
        public ChemicalInfo(String chemicalName, String casNumber) {
            this.chemicalName = chemicalName;
            this.casNumber = casNumber;
        }
        
        public String getChemicalName() {
            return chemicalName;
        }
        
        public String getCasNumber() {
            return casNumber;
        }
        
        public boolean hasCasNumber() {
            return casNumber != null && !casNumber.isEmpty();
        }
    }
}

