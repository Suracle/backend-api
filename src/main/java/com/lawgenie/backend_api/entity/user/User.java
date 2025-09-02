package com.lawgenie.backend_api.entity.user;

import com.lawgenie.backend_api.entity.base.BaseEntity;
import com.lawgenie.backend_api.entity.user.enums.UserType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_type", nullable = false, length = 20)
  private UserType userType;

  @Column(name = "user_name", length = 100)
  private String userName;

  @Column(name = "preferred_language", length = 5)
  private String preferredLanguage;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive;
}
