package com.example.prospera.courses.api.dto;

import com.example.prospera.courses.domain.Training;
import com.example.prospera.courses.domain.enums.PublicationStatus;
import com.example.prospera.courses.domain.enums.TrainingEntityType;

import java.util.UUID;

/**
 * DTO para uma visão resumida de um treinamento,
 * ideal para listagens administrativas.
 */
public record TrainingSummaryDTO(
        UUID id,
        String title,
        TrainingEntityType entityType,
        String author,
        PublicationStatus status
) {
    public static TrainingSummaryDTO fromEntity(Training training) {
        return new TrainingSummaryDTO(
                training.getId(),
                training.getTitle(),
                training.getEntityType(),
                training.getAuthor(),
                training.getStatus()
        );
    }
}