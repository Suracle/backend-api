package com.lawgenie.backend_api.entity;

import com.lawgenie.backend_api.entity.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "declaration_party")
public class DeclarationParty extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "export_declaration_id")
  private ExportDeclaration exportDeclaration;

  @ManyToOne
  @JoinColumn(name = "party_id")
  private Party party;

  private String roleOverride;
}
