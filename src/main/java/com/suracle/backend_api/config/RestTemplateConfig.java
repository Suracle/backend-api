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
        
        // 타임아웃 설정 (AI 분석은 시간이 오래 걸릴 수 있음)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 5초 연결 타임아웃
        factory.setReadTimeout(30000);    // 30초 읽기 타임아웃
        
        restTemplate.setRequestFactory(factory);
        
        // JSON 메시지 컨버터 추가
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        
        return restTemplate;
    }
}
