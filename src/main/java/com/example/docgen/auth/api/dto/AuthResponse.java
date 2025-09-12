package com.example.docgen.auth.api.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class AuthResponse {
    UUID userId;
    String email;
    String token;
    @Builder.Default
    String tokenType = "Bearer"; // Define "Bearer" como padrão
    long expiresIn;
    String accountType;
}