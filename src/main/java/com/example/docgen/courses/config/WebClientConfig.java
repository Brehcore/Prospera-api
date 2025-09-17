package com.example.docgen.courses.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${enterprise.service.url}")
    private String enterpriseServiceUrl;

    @Bean
    public WebClient enterpriseWebClient() {
        return WebClient.builder()
                .baseUrl(enterpriseServiceUrl)
                .build();
    }
}