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
@Table(name = "declaration_item")
public class DeclarationItem extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "export_declaration_id")
  private ExportDeclaration exportDeclaration;

  @ManyToOne
  @JoinColumn(name = "product_item_id")
  private ProductItem productItem;

  private String scheduleB;
  private String eccn;
  private String usmlCat;
  private Integer qty;
  private BigDecimal weightKg;
  private BigDecimal valueUsd;

  @Column(length = 3)
  private String originCountry;

  private String licenseException;

  @Lob
  private String notes;
}
