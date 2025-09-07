package com.suracle.backend_api.entity.chat;

import com.suracle.backend_api.entity.chat.enums.MessageSenderType;
import com.suracle.backend_api.entity.chat.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false)
  private ChatSession session;

  @Enumerated(EnumType.STRING)
  @Column(name = "sender_type", length = 10)
  private MessageSenderType senderType;

  @Column(name = "message_content", columnDefinition = "text")
  private String messageContent;

  @Enumerated(EnumType.STRING)
  @Column(name = "message_type", length = 10)
  private MessageType messageType;

  @Column(name = "metadata", columnDefinition = "text")
  private String metadata;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
