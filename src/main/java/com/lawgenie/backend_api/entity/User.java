package com.lawgenie.backend_api.entity;

import com.lawgenie.backend_api.entity.base.Base;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends Base {
  /** 역할 N:1, 게시판 1:N, 검색로그 1:N, 사용자데이터 1:1 */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String name;
  @Column(nullable = false, length = 100, unique = true)
  private String email;
  @Column(nullable = false)
  private String password;

  /** N:1 역할 */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_user_role"))
  private Role role;

}
