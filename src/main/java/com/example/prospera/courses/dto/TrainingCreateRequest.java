package com.example.prospera.courses.dto;

import com.example.prospera.courses.domain.enums.TrainingEntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TrainingCreateRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String author,
        @NotNull TrainingEntityType entityType, // EBOOK, RECORDED_COURSE, ou LIVE_TRAINING
        UUID organizationId // Opcional, para treinamentos exclusivos
) {
}