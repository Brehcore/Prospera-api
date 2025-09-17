package com.example.docgen.courses.api.dto;

import com.example.docgen.courses.domain.Course;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// DTO que representa um curso com seus módulos e lições aninhados
public record CourseDetailDTO(
        UUID id,
        String title,
        List<ModuleDetailDTO> modules
) {
    public static CourseDetailDTO fromEntity(Course course) {
        return new CourseDetailDTO(
                course.getId(),
                course.getTitle(),
                course.getModules().stream().map(ModuleDetailDTO::fromEntity).collect(Collectors.toList())
        );
    }

    // Sub-DTO para os módulos
    public record ModuleDetailDTO(UUID id, String title, int order, List<LessonSummaryDTO> lessons) {
        public static ModuleDetailDTO fromEntity(com.example.docgen.courses.domain.Module module) {
            return new ModuleDetailDTO(
                    module.getId(),
                    module.getTitle(),
                    module.getModuleOrder(),
                    module.getLessons().stream().map(LessonSummaryDTO::fromEntity).collect(Collectors.toList())
            );
        }
    }

    // Sub-DTO para as lições
    public record LessonSummaryDTO(UUID id, String title, int order) {
        public static LessonSummaryDTO fromEntity(com.example.docgen.courses.domain.Lesson lesson) {
            return new LessonSummaryDTO(lesson.getId(), lesson.getTitle(), lesson.getLessonOrder());
        }
    }


}