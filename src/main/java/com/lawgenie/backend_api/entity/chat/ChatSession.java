package com.lawgenie.backend_api.entity.chat;

import com.lawgenie.backend_api.entity.base.BaseEntity;
import com.lawgenie.backend_api.entity.chat.enums.ChatSessionStatus;
import com.lawgenie.backend_api.entity.chat.enums.ChatSessionType;
import com.lawgenie.backend_api.entity.user.User;
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

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private ChatSessionStatus status;

  @Column(name = "session_data", columnDefinition = "json")
  private String sessionData;
}
