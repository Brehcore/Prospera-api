package com.example.docgen.auth.dto;

import com.example.docgen.common.enums.UserType;

import java.util.UUID;

/**
 * DTO de resposta unificado para a consulta de perfis de usuário.
 */
public record ProfileResponseDTO(
        UUID userId,
        String email,
        String role,
        UserType userType,
        String name,      // Armazenará o Nome Completo ou a Razão Social
        String document,  // Armazenará o CPF ou CNPJ
        String phone      // Opcional
) {
}