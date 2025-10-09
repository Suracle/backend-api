package com.suracle.backend_api.entity.hs;

import com.suracle.backend_api.entity.base.BaseEntity;
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

  @Column(name = "description", columnDefinition = "text", nullable = false)
  private String description;

  @Column(name = "us_tariff_rate", precision = 5, scale = 4)
  private BigDecimal usTariffRate;

  @Column(name = "reasoning", columnDefinition = "text")
  private String reasoning;  // HS 코드 추천 근거

  @Column(name = "tariff_reasoning", columnDefinition = "text")
  private String tariffReasoning;  // 관세율 적용 근거

  @Column(name = "last_updated")
  private LocalDateTime lastUpdated;
}
