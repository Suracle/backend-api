package com.lawgenie.backend_api.entity;

import com.lawgenie.backend_api.entity.base.Base;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_logs", indexes = {
    @Index(name = "idx_searchlog_user", columnList = "user_id"),
    @Index(name = "idx_searchlog_time", columnList = "searchedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchLog extends Base {
  /** 회원 N:1 */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_searchlog_user"))
  private User user;

  @Column(nullable = false, length = 500)
  private String query;

  @Column(nullable = false)
  private Integer resultCount;

  @Column(nullable = false)
  private LocalDateTime searchedAt;
}
