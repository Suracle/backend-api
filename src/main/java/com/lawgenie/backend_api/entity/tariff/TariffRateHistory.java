package com.lawgenie.backend_api.entity.tariff;

import com.lawgenie.backend_api.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tariff_rate_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffRateHistory extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "hs_code", length = 15, nullable = false)
  private String hsCode;

  @Column(name = "origin_country", length = 3)
  private String originCountry;

  @Column(name = "tariff_rate", precision = 5, scale = 4)
  private BigDecimal tariffRate;

  @Column(name = "effective_from")
  private LocalDate effectiveFrom;

  @Column(name = "effective_to")
  private LocalDate effectiveTo;

  @Lob
  @Column(name = "change_reason")
  private String changeReason;
}
