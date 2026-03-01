package com.example.prospera.courses.dto;

import com.example.prospera.courses.domain.Training;
import com.example.prospera.courses.domain.enums.TrainingEntityType;

import java.util.UUID;

/**
 * DTO genérico para retornar informações básicas sobre um treinamento recém-criado ou atualizado.
 */
public record TrainingDTO(
        UUID id,
        String title,
        TrainingEntityType entityType
) {
    public static TrainingDTO fromEntity(Training training) {
        // Este método converte a entidade Training para este DTO.
        // Ele extrai o valor do 'DiscriminatorValue' para determinar o tipo.
        return new TrainingDTO(
                training.getId(),
                training.getTitle(),
                training.getEntityType()
        );
    }
}