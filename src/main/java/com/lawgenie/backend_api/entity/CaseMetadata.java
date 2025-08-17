package com.lawgenie.backend_api.entity;

import com.lawgenie.backend_api.entity.base.Base;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "case_metadata", indexes = {
    @Index(name = "idx_case_court_date", columnList = "courtName,decisionDate"),
    @Index(name = "idx_case_number", columnList = "caseNumber")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseMetadata extends Base {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 500)
  private String title;

  @Column(nullable = false, length = 100)
  private String courtName;

  @Column(nullable = false)
  private LocalDate decisionDate;

  @Column(nullable = false, length = 100)
  private String caseNumber;

  @Lob
  @Column(nullable = false)
  private String statutes;
}
