package com.example.prospera.courses.dto;

import com.example.prospera.courses.domain.Lesson;

import java.util.UUID;

public record LessonDTO(
        UUID id,
        String title,
        String content,
        int order,
        String videoUrl,
        int durationInMinutes,
        boolean isCompleted // Campo novo
) {

    // Metodo 1: Completo (usado quando sabemos o progresso)
    public static LessonDTO fromEntity(Lesson lesson, boolean isCompleted) {
        return new LessonDTO(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getContent(),
                lesson.getLessonOrder(),
                lesson.getVideoUrl(),
                lesson.getDurationInMinutes(),
                isCompleted
        );
    }

    // Metodo 2: Simplificado (usado pelo Next/Previous, assume false)
    // Corrige o erro "cannot be applied to given types"
    public static LessonDTO fromEntity(Lesson lesson) {
        return fromEntity(lesson, false);
    }
}