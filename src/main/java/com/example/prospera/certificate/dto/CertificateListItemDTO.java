package com.example.prospera.certificate.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CertificateListItemDTO(
        UUID certificateId,
        String courseTitle,
        String coverImageUrl,     // Para a imagem do card
        OffsetDateTime issuedAt,  // Data de conclusão
        String workload,          // Carga horária (ex: "10 horas")
        String validationCode,    // Código visível no card
        String downloadUrl        // Link direto para baixar
) {
}