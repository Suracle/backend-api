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
@Table(name = "document_link")
public class DocumentLink extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "document_id")
  private Document document;

  @ManyToOne
  @JoinColumn(name = "export_declaration_id")
  private ExportDeclaration exportDeclaration;

  @ManyToOne
  @JoinColumn(name = "declaration_item_id")
  private DeclarationItem declarationItem;

  @Lob
  private String notes;
}
