package com.example.prospera.courses.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ModuleCreateRequest(
        @NotBlank(message = "O título do módulo é obrigatório")
        String title,

        @NotNull(message = "A ordem do módulo é obrigatória")
        @PositiveOrZero(message = "A ordem deve ser um número positivo ou zero")
        Integer moduleOrder
) {
}