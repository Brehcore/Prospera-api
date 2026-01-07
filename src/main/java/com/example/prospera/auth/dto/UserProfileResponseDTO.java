package com.example.prospera.auth.dto;

import com.example.prospera.common.enums.UserType;

import java.util.UUID;


public record UserProfileResponseDTO(
        UUID userId,
        String email,
        String role,
        UserType userType,
        String name,      // Conterá o Nome Completo ou a Razão Social
        String document,  // Conterá o CPF ou CNPJ
        String phone      // Opcional
) {
}