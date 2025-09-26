package com.example.docgen.courses.api.dto;

import com.example.docgen.courses.domain.TrainingSectorAssignment;
import com.example.docgen.courses.domain.enums.TrainingType;

import java.util.UUID;

/**
 * DTO para representar a associação de um treinamento com um setor.
 */
public record TrainingSectorAssignmentDTO(
        UUID sectorId,
        TrainingType trainingType,
        String legalBasis
) {
    public static TrainingSectorAssignmentDTO fromEntity(TrainingSectorAssignment assignment) {
        return new TrainingSectorAssignmentDTO(
                assignment.getSectorId(),
                assignment.getTrainingType(),
                assignment.getLegalBasis()
        );
    }
}