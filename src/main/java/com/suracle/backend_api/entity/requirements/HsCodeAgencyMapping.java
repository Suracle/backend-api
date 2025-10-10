package com.suracle.backend_api.entity.requirements;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "hs_code_agency_mappings", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"hs_code", "product_category"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HsCodeAgencyMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "hs_code", nullable = false, length = 10)
    private String hsCode;
    
    @Column(name = "product_category", length = 100)
    private String productCategory;
    
    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recommended_agencies", columnDefinition = "JSONB", nullable = false)
    private String recommendedAgencies; // JSON 문자열로 저장 (Hibernate가 자동 변환)
    
    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore;
    
    @Column(name = "usage_count")
    private Integer usageCount;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
