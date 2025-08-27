package com.lawgenie.backend_api.entity;

import jakarta.persistence.*;
import java.util.List;

import com.lawgenie.backend_api.entity.base.BaseEntity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "export_declaration")
public class ExportDeclaration extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String docRef;

  @Column(length = 3)
  private String destCountry;

  private Boolean eouRequired;
  private Boolean licenseFlag;
  private String status;
  private String remarks;

  @OneToMany(mappedBy = "exportDeclaration", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DeclarationItem> items;

  @OneToMany(mappedBy = "exportDeclaration", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DeclarationParty> parties;

  @OneToMany(mappedBy = "exportDeclaration", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ComplianceCheck> complianceChecks;

  @OneToMany(mappedBy = "exportDeclaration", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RiskScreening> riskScreenings;

  @OneToMany(mappedBy = "exportDeclaration", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DutyPreview> dutyPreviews;

  @OneToMany(mappedBy = "exportDeclaration", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<EclGrant> eclGrants;

  @OneToMany(mappedBy = "exportDeclaration", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DocumentLink> documentLinks;
}
