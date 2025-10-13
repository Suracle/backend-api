package com.suracle.backend_api.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

@Service
public class ChatService {

  private final WebClient webClient;

  public ChatService(WebClient.Builder builder) {
    // 메인 AI 엔진(포트 8000)으로 통합됨
    this.webClient = builder.baseUrl("http://127.0.0.1:8000").build();
  }

  public Flux<String> toModel(String userInput) {
    return webClient.post()
        .uri("/chat/api")  // POST /chat/api
        .bodyValue(Map.of("sender", "user", "message", userInput))
        .retrieve()
        .bodyToFlux(String.class);
  }

}
