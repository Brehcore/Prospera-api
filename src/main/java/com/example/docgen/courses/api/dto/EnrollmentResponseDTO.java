package com.example.docgen.courses.api.dto;

import com.example.docgen.courses.domain.Enrollment;
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
    public static EnrollmentResponseDTO fromEntity(Enrollment enrollment) {
        return new EnrollmentResponseDTO(
                enrollment.getId(),
                enrollment.getTraining().getId(),
                enrollment.getTraining().getTitle(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt(),
                enrollment.getTraining().getCoverImageUrl(),
                enrollment.getProgressPercentage()
        );
    }
}