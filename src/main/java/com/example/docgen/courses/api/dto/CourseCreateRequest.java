package com.example.docgen.courses.api.dto;

import com.example.docgen.courses.domain.enums.ContentType;
import com.example.docgen.courses.domain.enums.CourseModality;
import com.example.docgen.courses.domain.enums.PublicationStatus;
import com.example.docgen.courses.domain.enums.TrainingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CourseCreateRequest(
        @NotBlank(message = "O título é obrigatório")
        String title,

        @NotBlank(message = "A descrição é obrigatória")
        String description,

        @NotNull(message = "O tipo de conteúdo é obrigatório")
        ContentType contentType,

        @NotBlank(message = "O nome do autor é obrigatório")
        String author,

        @NotNull(message = "O ID do setor é obrigatório")
        UUID setorId,

        @NotNull(message = "O tipo de modalidade é obrigatório")
        CourseModality modality,

        @NotNull(message = "O status da publicação é obrigatório")
        PublicationStatus publicationStatus,

        @NotNull(message = "O tipo de treinamento é obrigatório")
        TrainingType trainingType


) implements CourseRequestData {
}