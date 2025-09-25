package com.example.docgen.courses.api.dto;

import com.example.docgen.courses.domain.Training;

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
        String coverImageUrl // Campo para a imagem da capa
) {
    public static PublicTrainingDTO fromEntity(Training training) {
        // Supondo que você adicione um campo 'coverImageUrl' à sua entidade Training
        String imageUrl = (training.getCoverImageUrl() != null) ? training.getCoverImageUrl() : "url/de/imagem/padrao.jpg";

        return new PublicTrainingDTO(
                training.getId(),
                training.getTitle(),
                training.getAuthor(),
                training.getDescription(),
                imageUrl
        );
    }
}