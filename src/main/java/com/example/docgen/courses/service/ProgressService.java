package com.example.docgen.courses.service;

import com.example.docgen.courses.domain.EbookProgress;
import com.example.docgen.courses.domain.EbookTraining;
import com.example.docgen.courses.domain.Training;
import com.example.docgen.courses.domain.Enrollment;
import com.example.docgen.courses.domain.Lesson;
import com.example.docgen.courses.domain.LessonProgress;
import com.example.docgen.courses.domain.enums.EnrollmentStatus;
import com.example.docgen.courses.repositories.EbookProgressRepository;
import com.example.docgen.courses.repositories.EnrollmentRepository;
import com.example.docgen.courses.repositories.LessonProgressRepository;
import com.example.docgen.courses.repositories.LessonRepository;
import com.example.docgen.courses.repositories.TrainingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final EbookProgressRepository ebookProgressRepository;
    private final TrainingRepository trainingRepository;

    @Transactional
    public LessonProgress markLessonAsCompleted(UUID userId, UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lição não encontrada com o ID: " + lessonId));

        // CORREÇÃO 1: A entidade Module tem um campo 'course' (do tipo RecordedCourse)
        Training training = lesson.getModule().getCourse();

        // CORREÇÃO 2: O método no repositório foi renomeado para 'findByUserIdAndTrainingId'
        Enrollment enrollment = enrollmentRepository.findByUserIdAndTrainingId(userId, training.getId())
                .orElseThrow(() -> new IllegalStateException("Usuário com ID " + userId + " não está matriculado no treinamento."));

        if (lessonProgressRepository.existsByEnrollmentAndLesson(enrollment, lesson)) {
            throw new IllegalStateException("Esta lição já foi marcada como concluída.");
        }

        LessonProgress progress = LessonProgress.builder()
                .enrollment(enrollment)
                .lesson(lesson)
                .completedAt(OffsetDateTime.now())
                .build();

        LessonProgress savedProgress = lessonProgressRepository.save(progress);

        checkCourseCompletion(enrollment);

        return savedProgress;
    }

    private void checkCourseCompletion(Enrollment enrollment) {
        // CORREÇÃO 3: A entidade Enrollment tem um campo 'training'
        Training training = enrollment.getTraining();

        // Esta chamada para 'countByCourseId' pode ser renomeada para 'countByTrainingId' no futuro para maior clareza,
        // mas se estiver funcionando com a @Query, não há problema.
        long totalLessonsInCourse = lessonRepository.countByCourseId(training.getId());

        long completedLessonsForEnrollment = lessonProgressRepository.countByEnrollment(enrollment);

        if (totalLessonsInCourse > 0 && totalLessonsInCourse == completedLessonsForEnrollment) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollmentRepository.save(enrollment);
        }
    }

    @Transactional
    public void updateEbookProgress(UUID userId, UUID trainingId, int lastPageRead) {
        // Valida se o treinamento existe e é um Ebook
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado: " + trainingId));
        if (!(training instanceof EbookTraining)) {
            throw new IllegalArgumentException("Progresso só pode ser registrado para Ebooks.");
        }

        EbookProgress progress = ebookProgressRepository.findByUserIdAndTrainingId(userId, trainingId)
                .orElse(EbookProgress.builder().userId(userId).training(training).build());

        progress.setLastPageRead(lastPageRead);
        ebookProgressRepository.save(progress);
    }
}