package com.example.docgen.courses.api.dto;

import com.example.docgen.courses.domain.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CourseCreateRequest(
        @NotBlank(message = "O título é obrigatório")
        String title,

        @NotBlank(message = "A descrição é obrigatória")
        String description,

        @NotNull(message = "O tipo de conteúdo é obrigatório")
        ContentType contentType,

        @NotBlank(message = "O nome do autor é obrigatório")
        String author
) {
}