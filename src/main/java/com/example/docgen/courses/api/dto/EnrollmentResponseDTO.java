package com.example.docgen.courses.api.dto;

import com.example.docgen.courses.domain.enums.EnrollmentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record EnrollmentResponseDTO(
        UUID enrollmentId,
        UUID trainingId,
        String trainingTitle,
        EnrollmentStatus status,
        OffsetDateTime enrolledAt,
        String coverImageUrl,
        BigDecimal progressPercentage
) {
}