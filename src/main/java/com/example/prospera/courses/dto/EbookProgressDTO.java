package com.example.prospera.courses.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EbookProgressDTO(
        Integer lastPageRead,
        Integer totalPages,
        BigDecimal progressPercentage,
        OffsetDateTime lastUpdatedAt
) {
}
