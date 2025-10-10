package com.suracle.backend_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // 타임아웃 설정 (백그라운드 AI 분석: Tavily Search + 스크래핑 + GPT)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);   // 10초 연결 타임아웃
        factory.setReadTimeout(1200000);     // 600초 (10분) 읽기 타임아웃 - 백그라운드 작업
        
        restTemplate.setRequestFactory(factory);
        
        // JSON 메시지 컨버터 추가
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        
        // 공통 헤더 인터셉터 추가 (EPA CompTox, CPSC 등 헤더 요구 API 대응)
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Accept", "application/json");
            request.getHeaders().add("User-Agent", "LawGenie-Backend/1.0");
            return execution.execute(request, body);
        });
        
        return restTemplate;
    }
}
