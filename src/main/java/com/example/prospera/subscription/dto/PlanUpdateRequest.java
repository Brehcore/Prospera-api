package com.example.prospera.subscription.dto;

import com.example.prospera.subscription.enums.PlanType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PlanUpdateRequest(
        @NotBlank(message = "O nome não pode ser vazio")
        String name,

        @NotBlank(message = "A descrição não pode ser vazia")
        String description,

        @NotNull(message = "O preço original é obrigatório")
        @PositiveOrZero(message = "O preço não pode ser negativo")
        BigDecimal originalPrice,

        @NotNull(message = "O preço atual é obrigatório")
        @PositiveOrZero(message = "O preço não pode ser negativo")
        BigDecimal currentPrice,

        @NotNull(message = "A duração é obrigatória")
        @PositiveOrZero(message = "A duração não pode ser negativa")
        Integer durationInDays,

        @NotNull(message = "O status de ativação é obrigatório")
        Boolean isActive,

        @NotNull(message = "O tipo do plano (INDIVIDUAL ou ENTERPRISE) é obrigatório")
        PlanType type
) {
}