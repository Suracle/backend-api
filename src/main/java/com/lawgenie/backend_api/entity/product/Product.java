package com.lawgenie.backend_api.entity.product;

import com.lawgenie.backend_api.entity.base.BaseEntity;
import com.lawgenie.backend_api.entity.product.enums.ProductStatus;
import com.lawgenie.backend_api.entity.user.User;
import com.lawgenie.backend_api.entity.hs.HsCode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products", uniqueConstraints = @UniqueConstraint(columnNames = "product_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id", nullable = false)
  private User seller;

  @Column(name = "product_id", nullable = false, unique = true, length = 20)
  private String productId;

  @Column(name = "product_name", nullable = false, length = 255)
  private String productName;

  @Lob
  @Column(name = "description")
  private String description;

  @Column(precision = 19, scale = 4)
  private BigDecimal price;

  @Column(name = "fob_price", precision = 19, scale = 4)
  private BigDecimal fobPrice;

  @Column(name = "origin_country", length = 3)
  private String originCountry;

  @Column(name = "hs_code", length = 15)
  private String hsCodeValue;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private ProductStatus status;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hs_code", referencedColumnName = "hs_code", insertable = false, updatable = false)
  private HsCode hsCode;
}
