package com.example.prospera.courses.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record LessonCreateRequest(
        @NotBlank(message = "O título da lição é obrigatório")
        String title,

        String content, // Conteúdo pode ser opcional
        String videoUrl,

        @NotNull(message = "A ordem da lição é obrigatória")
        @PositiveOrZero(message = "A ordem deve ser um número positivo ou zero")
        Integer lessonOrder
) {
}