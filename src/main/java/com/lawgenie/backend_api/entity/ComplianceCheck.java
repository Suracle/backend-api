package com.lawgenie.backend_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.lawgenie.backend_api.entity.base.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "compliance_check")
public class ComplianceCheck extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "export_declaration_id")
  private ExportDeclaration exportDeclaration;

  private String result;
  private String exceptionCode;

  @Lob
  private String reasons; // JSON
  private String dtcRefs;
  private String decisions; // JSON
  private String policyVersion;

  private LocalDateTime decidedAt;
}
