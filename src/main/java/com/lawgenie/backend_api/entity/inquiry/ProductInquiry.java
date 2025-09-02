package com.lawgenie.backend_api.entity.inquiry;

import com.lawgenie.backend_api.entity.base.BaseEntity;
import com.lawgenie.backend_api.entity.inquiry.enums.InquiryType;
import com.lawgenie.backend_api.entity.product.Product;
import com.lawgenie.backend_api.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_inquiries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductInquiry extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  @Enumerated(EnumType.STRING)
  @Column(name = "inquiry_type", length = 20)
  private InquiryType inquiryType;

  @Column(name = "inquiry_data", columnDefinition = "json")
  private String inquiryData;

  @Lob
  @Column(name = "ai_response")
  private String aiResponse;
}
