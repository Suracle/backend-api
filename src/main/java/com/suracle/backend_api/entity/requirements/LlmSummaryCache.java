package com.suracle.backend_api.entity.requirements;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "llm_summary_cache",
       uniqueConstraints = @UniqueConstraint(columnNames = {"hs_code", "product_name", "raw_documents_hash"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmSummaryCache {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "hs_code", nullable = false, length = 10)
    private String hsCode;
    
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;
    
    @Column(name = "raw_documents_hash", nullable = false, length = 64)
    private String rawDocumentsHash;
    
    @Column(name = "summary_result", nullable = false, columnDefinition = "JSONB")
    private String summaryResult; // JSON 문자열로 저장
    
    @Column(name = "model_used", nullable = false, length = 50)
    private String modelUsed;
    
    @Column(name = "tokens_used")
    private Integer tokensUsed;
    
    @Column(name = "cost", precision = 10, scale = 6)
    private BigDecimal cost;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
