package com.lawgenie.backend_api.entity.ai;

import com.lawgenie.backend_api.entity.base.BaseEntity;
import com.lawgenie.backend_api.entity.ai.enums.AiReportStatus;
import com.lawgenie.backend_api.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_analysis_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiAnalysisReport extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "report_type", length = 20)
  private String reportType;

  @Lob
  @Column(name = "analysis_content")
  private String analysisContent;

  @Lob
  @Column(name = "ai_reasoning")
  private String aiReasoning;

  @Column(columnDefinition = "json")
  private String metadata;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private AiReportStatus status;
}
