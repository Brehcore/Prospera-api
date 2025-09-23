package com.example.docgen.enterprise.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SectorIdRequest(
        @NotNull(message = "O ID do setor é obrigatório")
        UUID sectorId
) {
}