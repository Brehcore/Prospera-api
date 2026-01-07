package com.example.prospera.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO imutável para a requisição de login.
 * Usa record para concisão e segurança.
 */
public record AuthLoginRequest(
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        String password
) {
}