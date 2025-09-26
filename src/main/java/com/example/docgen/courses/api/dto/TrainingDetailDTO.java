package com.example.docgen.courses.api.dto;

import com.example.docgen.courses.domain.EbookTraining;
import com.example.docgen.courses.domain.LiveTraining;
import com.example.docgen.courses.domain.RecordedCourse;
import com.example.docgen.courses.domain.Training;
import com.example.docgen.courses.domain.enums.PublicationStatus;
import com.example.docgen.courses.domain.enums.TrainingEntityType;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record TrainingDetailDTO(
        UUID id,
        String title,
        String description,
        TrainingEntityType entityType,
        String author,
        PublicationStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String coverImageUrl,

        // Campos Polimórficos
        EbookDetails ebookDetails, // Nulo se não for um Ebook
        CourseDetails courseDetails, // Nulo se não for um Curso Gravado
        LiveDetails liveDetails, // Nulo se não for um Treinamento ao Vivo

        // NOVO CAMPO:
        List<TrainingSectorAssignmentDTO> sectorAssignments // Lista de associações com setores

) {
    public static TrainingDetailDTO fromEntity(Training training) {
        EbookDetails ebook = null;
        CourseDetails course = null;
        LiveDetails live = null;

        if (training instanceof EbookTraining et) {
            ebook = new EbookDetails(et.getFilePath(), et.getTotalPages(), et.getFileUploadedAt());
        } else if (training instanceof RecordedCourse rc) {
            course = new CourseDetails(rc.getModules().stream().map(CourseDetails.ModuleDetail::fromEntity).collect(Collectors.toList()));
        } else if (training instanceof LiveTraining lt) {
            live = new LiveDetails(lt.getMeetingUrl(), lt.getStartDateTime());
        }

        // LÓGICA ADICIONAL: Buscar as associações com setores
        List<TrainingSectorAssignmentDTO> assignments = training.getSectorAssignments() != null
                ? training.getSectorAssignments().stream()
                .map(TrainingSectorAssignmentDTO::fromEntity)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return new TrainingDetailDTO(
                training.getId(),
                training.getTitle(),
                training.getDescription(),
                training.getEntityType(),
                training.getAuthor(),
                training.getStatus(),
                training.getCreatedAt(),
                training.getUpdatedAt(),
                training.getCoverImageUrl(),

                // Campos Polimórficos
                ebook,
                course,
                live,
                assignments // Lista de associações com setores
        );
    }

    // Sub-records para detalhes específicos de cada tipo
    public record EbookDetails(String filePath, Integer totalPages, OffsetDateTime fileUploadedAt) {
    }

    public record LiveDetails(String meetingUrl, OffsetDateTime startDateTime) {
    }

    public record CourseDetails(List<ModuleDetail> modules) {
        public record ModuleDetail(UUID id, String title, int order, List<LessonSummary> lessons) {
            public static ModuleDetail fromEntity(com.example.docgen.courses.domain.Module module) {
                return new ModuleDetail(
                        module.getId(),
                        module.getTitle(),
                        module.getModuleOrder(),
                        module.getLessons().stream().map(LessonSummary::fromEntity).collect(Collectors.toList())
                );
            }
        }

        public record LessonSummary(UUID id, String title, int order) {
            public static LessonSummary fromEntity(com.example.docgen.courses.domain.Lesson lesson) {
                return new LessonSummary(lesson.getId(), lesson.getTitle(), lesson.getLessonOrder());
            }
        }
    }
}