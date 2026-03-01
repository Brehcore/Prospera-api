package com.example.prospera.courses.dto;

import com.example.prospera.courses.domain.enums.TrainingType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TrainingSectorAssignmentRequest(
        @NotNull UUID sectorId,
        @NotNull TrainingType trainingType, // COMPULSORY ou ELECTIVE
        String legalBasis
) {
}