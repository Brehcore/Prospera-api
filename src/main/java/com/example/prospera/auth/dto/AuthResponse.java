package com.example.prospera.auth.dto;

import com.example.prospera.common.enums.UserRole;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class AuthResponse {
    UUID userId;
    String email;
    UserRole role;
    String token;
    @Builder.Default
    String tokenType = "Bearer"; // Define "Bearer" como padrão
    long expiresIn;
    String accountType;
}