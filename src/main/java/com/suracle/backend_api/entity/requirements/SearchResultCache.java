package com.suracle.backend_api.entity.requirements;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_result_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultCache {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "hs_code", nullable = false, length = 10)
    private String hsCode;
    
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;
    
    @Column(name = "agency", nullable = false, length = 20)
    private String agency;
    
    @Column(name = "search_query", nullable = false, columnDefinition = "TEXT")
    private String searchQuery;
    
    @Column(name = "search_results", nullable = false, columnDefinition = "JSONB")
    private String searchResults; // JSON 문자열로 저장
    
    @Column(name = "cache_key", nullable = false, unique = true, length = 255)
    private String cacheKey;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
