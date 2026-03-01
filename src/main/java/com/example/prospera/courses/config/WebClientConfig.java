package com.example.prospera.courses.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${enterprise.service.url}")
    private String enterpriseServiceUrl;

    @Bean
    public WebClient enterpriseWebClient() {
        return WebClient.builder()
                .baseUrl(enterpriseServiceUrl)
                .filter(addAuthorizationHeader()) // <-- Adiciona o filtro de autorização
                .build();
    }

    private ExchangeFilterFunction addAuthorizationHeader() {
        return (clientRequest, next) -> {
            // Pega o cabeçalho 'Authorization' da requisição original que chegou no controllers
            String authorizationHeader = getAuthorizationHeaderFromCurrentRequest();

            // Se houver um cabeçalho, clona a requisição do WebClient e adiciona ele
            if (authorizationHeader != null) {
                ClientRequest authorizedRequest = ClientRequest.from(clientRequest)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .build();
                return next.exchange(authorizedRequest);
            }

            // Se não houver, segue com a requisição original
            return next.exchange(clientRequest);
        };
    }

    private String getAuthorizationHeaderFromCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }
}