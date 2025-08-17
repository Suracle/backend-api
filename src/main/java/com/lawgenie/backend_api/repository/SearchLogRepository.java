package com.lawgenie.backend_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lawgenie.backend_api.entity.SearchLog;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

}
