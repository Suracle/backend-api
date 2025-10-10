package com.suracle.backend_api.entity.requirements;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "agency_search_strategies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgencySearchStrategy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "agency_name", nullable = false, unique = true, length = 20)
    private String agencyName;
    
    @Column(name = "search_provider", nullable = false, length = 20)
    private String searchProvider; // 'free_api', 'tavily', 'hybrid'
    
    @Column(name = "api_endpoint", length = 255)
    private String apiEndpoint;
    
    @Column(name = "api_key_required")
    private Boolean apiKeyRequired;
    
    @Column(name = "rate_limit_per_hour")
    private Integer rateLimitPerHour;
    
    @Column(name = "cost_per_request", precision = 10, scale = 6)
    private BigDecimal costPerRequest;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
