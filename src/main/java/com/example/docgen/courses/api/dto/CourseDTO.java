package com.example.docgen.courses.api.dto;

import com.example.docgen.courses.domain.Course;
import com.example.docgen.courses.domain.enums.ContentType;

import java.util.UUID;

public record CourseDTO(
        UUID id,
        String title,
        String description,
        String author,
        ContentType contentType,
        UUID sectorId
) {
    public static CourseDTO fromEntity(Course course) {
        return new CourseDTO(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getAuthor(),
                course.getContentType(),
                course.getSectorId()
        );
    }
}