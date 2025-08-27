package com.lawgenie.backend_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

import com.lawgenie.backend_api.entity.base.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "duty_preview")
public class DutyPreview extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "export_declaration_id")
  private ExportDeclaration exportDeclaration;

  @Column(length = 3)
  private String targetCountry;

  private BigDecimal baseTariffRate;

  private String importTaxes; // JSON
  private String fees; // JSON
  private String logisticsEst; // JSON

  private Boolean lspOption;
  private Boolean drawbackOption;

  @Lob
  private String notes;
}
