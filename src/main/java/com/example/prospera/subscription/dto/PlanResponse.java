package com.example.prospera.subscription.dto;

import com.example.prospera.subscription.entities.Plan;
import com.example.prospera.subscription.enums.PlanType;

import java.math.BigDecimal;
import java.util.UUID;

public record PlanResponse(
        UUID id,
        String name,
        String description,
        BigDecimal originalPrice,
        BigDecimal currentPrice,
        int durationInDays, // Usando 'int' primitivo, como na sua entidade
        boolean isActive,
        PlanType type
) {
    /**
     * Converte uma entidade Plan para este DTO.
     */
    public static PlanResponse fromEntity(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getDescription(),
                plan.getOriginalPrice(),
                plan.getCurrentPrice(),
                plan.getDurationInDays(),
                plan.isActive(),
                plan.getType()
        );
    }
}