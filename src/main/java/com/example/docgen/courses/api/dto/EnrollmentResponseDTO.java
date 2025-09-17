package com.example.docgen.courses.api.dto;

import com.example.docgen.courses.domain.Enrollment;
import com.example.docgen.courses.domain.enums.EnrollmentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EnrollmentResponseDTO(
        UUID enrollmentId,
        UUID courseId,
        String courseTitle,
        EnrollmentStatus status,
        OffsetDateTime enrolledAt
) {
    public static EnrollmentResponseDTO fromEntity(Enrollment enrollment) {
        return new EnrollmentResponseDTO(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt()
        );
    }
}