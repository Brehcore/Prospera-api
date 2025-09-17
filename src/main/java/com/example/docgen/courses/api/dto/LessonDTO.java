package com.example.docgen.courses.api.dto;

import com.example.docgen.courses.domain.Lesson;

import java.util.UUID;

public record LessonDTO(UUID id, String title, String content, int order) {
    public static LessonDTO fromEntity(Lesson lesson) {
        return new LessonDTO(lesson.getId(), lesson.getTitle(), lesson.getContent(), lesson.getLessonOrder());
    }
}