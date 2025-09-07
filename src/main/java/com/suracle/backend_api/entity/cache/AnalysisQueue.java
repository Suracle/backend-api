package com.suracle.backend_api.entity.cache;

import com.suracle.backend_api.entity.base.BaseEntity;
import com.suracle.backend_api.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_queue")
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisQueue extends BaseEntity  {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "analysis_types", nullable = false, columnDefinition = "json")
  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
  private String analysisTypes; // ['tariff_1qty', 'tariff_10qty', 'requirements', 'precedents']

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20)
  private QueueStatus status; // pending, processing, completed, failed

  @Column(name = "priority")
  private Integer priority;

  @Column(name = "scheduled_at")
  private LocalDateTime scheduledAt;

  @Column(name = "started_at")
  private LocalDateTime startedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "retry_count")
  private Integer retryCount;

  public enum QueueStatus {
    PENDING, PROCESSING, COMPLETED, FAILED
  }
}
