package com.example.prospera.courses.dto;

import com.example.prospera.courses.domain.enums.EnrollmentStatus;

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
        BigDecimal progressPercentage,
        UUID certificateId,
        String validationCode,
        Integer userRating // null se não avaliou, 1 a 5 se avaliou
) {
}