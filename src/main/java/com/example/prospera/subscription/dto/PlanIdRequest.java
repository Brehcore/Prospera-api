package com.example.prospera.subscription.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PlanIdRequest(
        @NotNull(message = "O ID do plano é obrigatório")
        UUID planId
) {
}