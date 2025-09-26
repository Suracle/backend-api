package com.suracle.backend_api.entity.cache;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.suracle.backend_api.entity.base.BaseEntity;
import com.suracle.backend_api.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_analysis_cache", uniqueConstraints = @UniqueConstraint(columnNames = { "product_id",
    "analysis_type" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAnalysisCache extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "analysis_type", nullable = false, length = 50)
  private String analysisType; // 'tariff_1qty', 'tariff_10qty', 'requirements', 'precedents'

  @Column(name = "analysis_result", nullable = false, columnDefinition = "json")
  @JdbcTypeCode(SqlTypes.JSON)
  private JsonNode analysisResult;

  @Column(name = "sources", nullable = false, columnDefinition = "json")
  @JdbcTypeCode(SqlTypes.JSON)
  private JsonNode sources;

  @Column(name = "confidence_score", precision = 3, scale = 2)
  private BigDecimal confidenceScore;

  @Column(name = "is_valid", nullable = false)
  private Boolean isValid;
}
