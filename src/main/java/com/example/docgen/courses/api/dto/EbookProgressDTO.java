package com.example.docgen.courses.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EbookProgressDTO(
        Integer lastPageRead,
        Integer totalPages,
        BigDecimal progressPercentage,
        OffsetDateTime lastUpdatedAt
) {
}
