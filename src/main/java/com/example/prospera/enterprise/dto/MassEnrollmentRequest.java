package com.example.prospera.enterprise.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record MassEnrollmentRequest(
        @NotNull(message = "O ID do treinamento é obrigatório.")
        UUID trainingId,

        @NotEmpty(message = "A lista de IDs de usuário não pode ser vazia.")
        List<UUID> userIds
) {
}