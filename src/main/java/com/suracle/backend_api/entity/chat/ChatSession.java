package com.suracle.backend_api.entity.chat;

import com.suracle.backend_api.entity.base.BaseEntity;
import com.suracle.backend_api.entity.chat.enums.ChatSessionStatus;
import com.suracle.backend_api.entity.chat.enums.ChatSessionType;
import com.suracle.backend_api.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "session_type", length = 20)
  private ChatSessionType sessionType;

  @Column(name = "language", length = 5, nullable = false)
  private String language;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private ChatSessionStatus status;

  @Column(name = "session_data", columnDefinition = "json")
  private String sessionData;
}
