package com.example.docgen.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para a requisição de criação de um perfil de Pessoa Física.
 */
public record UserProfilePFRequest(
        @NotBlank(message = "O nome completo é obrigatório")
        String fullName,

        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(regexp = "^[0-9]{11}$", message = "O CPF deve conter 11 dígitos")
        String cpf,

        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "A data de nascimento deve estar no formato AAAA-MM-DD")
        String birthDate,

        @Size(min = 8, max = 20, message = "Telefone deve ter entre 8 e 20 caracteres")
        String phone // Opcional, pode não ter @NotBlank se não for obrigatório
) {
}