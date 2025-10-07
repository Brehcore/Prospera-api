package com.example.docgen.courses.api.dto;


import com.example.docgen.courses.domain.enums.TrainingEntityType;

import java.util.List;
import java.util.UUID;

/**
 * DTO para a exibição pública de um treinamento.
 * Contém apenas informações de "vitrine".
 */
public record PublicTrainingDTO(
        UUID id,
        String title,
        String author,
        String description,
        String coverImageUrl, // Campo para a imagem da capa
        TrainingEntityType entityType,
        List<SimpleSectorDTO> sectors
) {
}