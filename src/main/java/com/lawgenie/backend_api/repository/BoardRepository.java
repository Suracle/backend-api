package com.lawgenie.backend_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lawgenie.backend_api.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {

}
