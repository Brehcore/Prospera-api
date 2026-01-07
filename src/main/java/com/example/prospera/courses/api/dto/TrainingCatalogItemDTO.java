package com.example.prospera.courses.api.dto;

import com.example.prospera.courses.domain.enums.EnrollmentStatus;
import com.example.prospera.courses.domain.enums.TrainingType;

import java.util.UUID;

/**
 * DTO que representa um item no catálogo de treinamentos disponível para um usuário específico.
 * Ele consolida informações do treinamento e do progresso do usuário.
 */
public record TrainingCatalogItemDTO(
        UUID trainingId,
        String title,
        String description,
        String author,
        String trainingEntityType, // "EBOOK", "COURSE", "LIVE" - para o frontend saber como renderizar
        TrainingType consolidatedTrainingType, // O tipo consolidado para o usuário (COMPULSORY ou ELECTIVE)
        EnrollmentStatus enrollmentStatus // Status da matrícula do usuário (NOT_ENROLLED, ACTIVE, COMPLETED)
) {
}