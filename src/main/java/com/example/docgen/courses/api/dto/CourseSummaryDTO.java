package com.example.docgen.courses.api.dto;

import com.example.docgen.courses.domain.Course;
import com.example.docgen.courses.domain.enums.ContentType;
import com.example.docgen.courses.domain.enums.CourseModality;
import com.example.docgen.courses.domain.enums.PublicationStatus;
import com.example.docgen.courses.domain.enums.TrainingType;

import java.util.UUID;

public record CourseSummaryDTO(
        UUID id,
        String title,
        String description,
        ContentType contentType,
        String author,
        String sectorName,
        CourseModality modality,
        PublicationStatus publicationStatus,
        TrainingType trainingType
) {
    public static CourseSummaryDTO fromEntity(Course course, String sectorName) {
        return new CourseSummaryDTO(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getContentType(),
                course.getAuthor(),
                sectorName,
                course.getModality(),
                course.getStatus(),
                course.getTrainingType()
        );
    }
}