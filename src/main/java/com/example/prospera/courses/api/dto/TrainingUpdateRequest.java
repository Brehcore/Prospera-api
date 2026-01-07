package com.example.prospera.courses.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para a requisição de atualização de um treinamento.
 * Contém os campos que podem ser modificados por um SYSTEM_ADMIN.
 */
public record TrainingUpdateRequest(
        @NotBlank(message = "O título não pode estar em branco.")
        @Size(max = 255, message = "O título deve ter no máximo 255 caracteres.")
        String title,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres.")
        String description,

        @Size(max = 255, message = "O nome do autor deve ter no máximo 255 caracteres.")
        String author
) {
}