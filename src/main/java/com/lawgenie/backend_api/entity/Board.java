package com.lawgenie.backend_api.entity;

import com.lawgenie.backend_api.entity.base.Base;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "boards", indexes = {
    @Index(name = "idx_boards_author", columnList = "author_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board extends Base {
  /** 작성자(회원) N:1 */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String title;

  @Lob
  @Column(nullable = false)
  private String content;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_id", foreignKey = @ForeignKey(name = "fk_board_user"))
  private User author;
}