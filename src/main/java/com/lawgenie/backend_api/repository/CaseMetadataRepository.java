package com.lawgenie.backend_api.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lawgenie.backend_api.entity.CaseMetadata;

public interface CaseMetadataRepository extends JpaRepository<CaseMetadata, Long> {
  List<CaseMetadata> findByCourtNameAndDecisionDate(String courtName, LocalDate decisionDate);

  List<CaseMetadata> findByCaseNumber(String caseNumber);
}
