package com.lawgenie.backend_api.entity.broker;

import com.lawgenie.backend_api.entity.base.BaseEntity;
import com.lawgenie.backend_api.entity.broker.enums.ReviewStatus;
import com.lawgenie.backend_api.entity.product.Product;
import com.lawgenie.backend_api.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "broker_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrokerReview extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "broker_id", nullable = false)
  private User broker;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_status", length = 20)
  private ReviewStatus reviewStatus;

  @Lob
  @Column(name = "review_comment")
  private String reviewComment;

  @Column(name = "requested_at")
  private LocalDateTime requestedAt;

  @Column(name = "reviewed_at")
  private LocalDateTime reviewedAt;
}
