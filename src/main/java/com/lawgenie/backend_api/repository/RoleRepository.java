package com.lawgenie.backend_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lawgenie.backend_api.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(String name);
}
