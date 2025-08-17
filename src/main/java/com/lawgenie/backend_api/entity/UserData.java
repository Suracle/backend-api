package com.lawgenie.backend_api.entity;

import com.lawgenie.backend_api.entity.base.Base;

import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "user_data", uniqueConstraints = {
    // 회원당 하나만
    @UniqueConstraint(name = "uk_userdata_user", columnNames = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserData extends Base {
  /** 회원 1:1, 판례와 N:M(북마크) */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_userdata_user"))
  private User user;

  @Lob
  private String searchHistory;

  @Lob
  private String settings;

  @ManyToMany
  @JoinTable(name = "user_bookmark", joinColumns = @JoinColumn(name = "user_data_id", foreignKey = @ForeignKey(name = "fk_bookmark_userdata")), inverseJoinColumns = @JoinColumn(name = "case_id", foreignKey = @ForeignKey(name = "fk_bookmark_case")))
  @Builder.Default
  private Set<CaseMetadata> bookmarks = new LinkedHashSet<>();

}
