package com.lawgenie.backend_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lawgenie.backend_api.entity.User;
import com.lawgenie.backend_api.entity.UserData;

public interface UserDataRepository extends JpaRepository<UserData, Long> {
  Optional<UserData> findByUser(User user);
}
