package com.lawgenie.backend_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

import com.lawgenie.backend_api.entity.base.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "document")
public class Document extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String docType;
  private String filename;

  @Lob
  private String content;

  private String hash;
  private LocalDate issuedAt;
  private String status;

  @Lob
  private String meta; // JSON
}
