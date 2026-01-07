package com.example.prospera.enterprise.api.dto;

import com.example.prospera.enterprise.domain.Sector;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

// DTO para criar, atualizar e responder
public record SectorDTO(
        UUID id,
        @NotBlank(message = "O nome do setor é obrigatório")
        String name
) {

    public static SectorDTO fromEntity(Sector sector) {
        return new SectorDTO(sector.getId(), sector.getName());
    }
}