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
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String subjectType;
  private Long subjectId;

  @Lob
  private String changes; // JSON

  private String policyVersion;
  private String actor;
}
