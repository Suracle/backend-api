package com.lawgenie.backend_api.entity.hs;

import com.lawgenie.backend_api.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "hs_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HsCode extends BaseEntity {

  @Id
  @Column(name = "hs_code", length = 20)
  private String hsCode;

  @Lob
  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "us_tariff_rate", precision = 5, scale = 4)
  private BigDecimal usTariffRate;

  @Lob
  @Column(name = "requirements")
  private String requirements;

  @Column(name = "trade_agreements", columnDefinition = "json")
  private String tradeAgreements;

  @Column(name = "last_updated")
  private LocalDateTime lastUpdated;
}
