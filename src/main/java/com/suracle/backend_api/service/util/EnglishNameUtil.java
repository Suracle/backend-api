package com.suracle.backend_api.service.util;

public final class EnglishNameUtil {
    private EnglishNameUtil() {}

    public static String toEnglishQuery(String productNameKorean) {
        if (productNameKorean == null || productNameKorean.isBlank()) {
            return "";
        }
        String lower = productNameKorean.toLowerCase();

        // 1) Exact/common mappings (align with Python mapping)
        if (lower.contains("노트북") || lower.contains("랩탑")) {
            return "laptop computer";
        }
        if (lower.contains("컴퓨터")) {
            return "computer";
        }
        if (lower.contains("비타민c") || lower.contains("vitamin c") || lower.contains("비타민 c") || lower.contains("ascorbic")) {
            return "vitamin c serum";
        }
        if (lower.contains("세럼")) {
            return "serum";
        }
        if (lower.contains("비타민")) {
            return "vitamin";
        }
        if (lower.contains("의료기기")) {
            return "medical device";
        }
        if (lower.contains("의약품")) {
            return "pharmaceutical";
        }
        if (lower.contains("식품")) {
            return "food";
        }
        if (lower.contains("화장품")) {
            return "cosmetic";
        }

        // 2) ASCII-only fallback (preserve ASCII tokens, drop others)
        StringBuilder sb = new StringBuilder();
        for (char ch : productNameKorean.toCharArray()) {
            if (ch < 128) sb.append(ch); else sb.append(' ');
        }
        String ascii = sb.toString().trim().replaceAll("\\s+", " ");
        if (!ascii.isBlank()) {
            return ascii;
        }

        // 3) Last resort: return a generic token
        return "product";
    }
}


