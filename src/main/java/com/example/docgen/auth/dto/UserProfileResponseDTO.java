package com.example.docgen.auth.dto;

import com.example.docgen.common.enums.UserType;

import java.util.UUID;

/**
 * DTO de resposta unificado e flat para a consulta de perfis.
 * Substitui o antigo ProfileResponseDTO.
 */
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