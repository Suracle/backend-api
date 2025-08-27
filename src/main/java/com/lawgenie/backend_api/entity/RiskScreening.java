package com.lawgenie.backend_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.lawgenie.backend_api.entity.base.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "risk_screening")
public class RiskScreening extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "export_declaration_id")
  private ExportDeclaration exportDeclaration;

  private String deniedPartyHits; // JSON
  private BigDecimal licenseRisk;
  private BigDecimal forcedLaborRisk;

  private String rulingRts; // JSON
  private LocalDateTime screenedAt;
}
