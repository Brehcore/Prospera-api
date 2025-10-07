package com.example.docgen.subscription.dto;

import java.math.BigDecimal;
import java.util.UUID;


public record PlanResponse(
        UUID id,
        String name,
        String description,
        BigDecimal originalPrice,
        BigDecimal currentPrice,
        Integer durationInDays,
        boolean isActive
) {
}
