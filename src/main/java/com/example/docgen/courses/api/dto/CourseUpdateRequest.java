package com.example.docgen.courses.api.dto;

import com.example.docgen.courses.domain.enums.ContentType;
import com.example.docgen.courses.domain.enums.CourseModality;
import com.example.docgen.courses.domain.enums.PublicationStatus;
import com.example.docgen.courses.domain.enums.TrainingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Similar ao Create, mas para o método PUT
public record CourseUpdateRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull ContentType contentType,
        @NotBlank String author,
        @NotNull UUID setorId,
        @NotNull CourseModality modality,
        @NotNull PublicationStatus publicationStatus,
        @NotNull TrainingType trainingType
) implements CourseRequestData {
}