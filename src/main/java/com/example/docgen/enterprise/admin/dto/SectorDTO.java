package com.example.docgen.enterprise.admin.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

// DTO para criar, atualizar e responder
public record SectorDTO(
        UUID id,
        @NotBlank(message = "O nome do setor é obrigatório")
        String name
) {
}