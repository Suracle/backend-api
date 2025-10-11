package com.suracle.backend_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.suracle.backend_api.websocket.ChatWebSoecketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  private final ChatWebSoecketHandler chatWebSoecketHandler;

  public WebSocketConfig(ChatWebSoecketHandler chatWebSoecketHandler) {
    this.chatWebSoecketHandler = chatWebSoecketHandler;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(chatWebSoecketHandler, "/ws/chat")
        .setAllowedOrigins("*");
  }

}
