package com.example.prospera.auth.api.dto;

import com.example.prospera.common.enums.UserRole;

import java.util.UUID;

/**
 * DTO (Data Transfer Object) para expor dados de um usuário de forma segura
 * para outros módulos da aplicação. Implementado como um record para concisão.
 */
public record UserDTO(
        UUID id,
        String name,
        String email,
        UserRole role
) {
}