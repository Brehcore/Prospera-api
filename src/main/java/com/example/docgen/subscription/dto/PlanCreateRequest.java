package com.example.docgen.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PlanCreateRequest(

        @NotBlank(message = "O nome do plano é obrigatório")
        String name,

        @NotBlank(message = "A descrição do plano é obrigatória")
        String description,

        @NotNull(message = "O Valor Original do plano é obrigatório")
        @PositiveOrZero(message = "O Valor Original do plano deve ser um valor positivo ou zero")
        BigDecimal originalPrice,

        @NotNull(message = "O Valor atual do plano é obrigatório")
        @PositiveOrZero(message = "O Valor atual do plano deve ser um valor positivo ou zero")
        BigDecimal currentPrice,

        @NotNull(message = "O período de tempo do plano é obrigatório")
        @PositiveOrZero(message = "O período de tempo do plano deve ser um valor positivo ou zero")
        Integer durationInDays
) {
}
