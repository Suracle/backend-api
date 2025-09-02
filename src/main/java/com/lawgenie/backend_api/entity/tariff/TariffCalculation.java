package com.lawgenie.backend_api.entity.tariff;

import com.lawgenie.backend_api.entity.product.Product;
import com.lawgenie.backend_api.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tariff_calculations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffCalculation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "buyer_id", nullable = false)
  private User buyer;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "total_value", precision = 19, scale = 4)
  private BigDecimal totalValue;

  @Column(name = "tariff_rate", precision = 5, scale = 4)
  private BigDecimal tariffRate;

  @Column(name = "tariff_amount", precision = 19, scale = 4)
  private BigDecimal tariffAmount;

  @Column(name = "total_with_tariff", precision = 19, scale = 4)
  private BigDecimal totalWithTariff;

  @Column(name = "calculated_at", nullable = false)
  private LocalDateTime calculatedAt;
}
