package com.example.docgen.auth.api.dto;

import com.example.docgen.common.enums.UserRole;
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