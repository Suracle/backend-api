package com.suracle.backend_api.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.suracle.backend_api.service.ChatService;

import lombok.NonNull;
import reactor.core.publisher.Flux;

@Component
public class ChatWebSoecketHandler extends TextWebSocketHandler {

  private final ChatService chatService;
  private final Map<WebSocketSession, Boolean> sessionStream = new ConcurrentHashMap<>();
  private static Logger logger = LoggerFactory.getLogger(ChatWebSoecketHandler.class);

  public ChatWebSoecketHandler(ChatService chatService) {
    this.chatService = chatService;
  }

  @Override
  public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
    String userInput = message.getPayload();

    if (sessionStream.getOrDefault(session, false)) {
      session.sendMessage(new TextMessage("아직 응답 중입니다."));
      return;
    }

    sessionStream.put(session, true);

    Flux<String> resposeFlux = chatService.toModel(userInput);

    resposeFlux
        .doOnNext(token -> {
          try {
            session.sendMessage(new TextMessage(token));
          } catch (Exception e) {
            e.printStackTrace();
          }
        })
        .doFinally(signalType -> {
          try {
            logger.info("응답 끝 -> SOCKET_CLOSE");
            String endMessage = "{\"message\":\"SOCKET_CLOSE\"}";
            session.sendMessage(new TextMessage(endMessage));
            sessionStream.put(session, false);
          } catch (Exception e) {
            e.printStackTrace();
          }
          sessionStream.put(session, false);
        })
        .subscribe();
  }

  @Override
  public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
    sessionStream.remove(session);
  }

}
