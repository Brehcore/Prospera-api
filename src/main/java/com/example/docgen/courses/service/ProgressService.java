package com.example.docgen.courses.service;

import com.example.docgen.courses.domain.Course;
import com.example.docgen.courses.domain.Enrollment;
import com.example.docgen.courses.domain.Lesson;
import com.example.docgen.courses.domain.LessonProgress;
import com.example.docgen.courses.domain.enums.EnrollmentStatus;
import com.example.docgen.courses.repositories.EnrollmentRepository;
import com.example.docgen.courses.repositories.LessonProgressRepository;
import com.example.docgen.courses.repositories.LessonRepository;
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

    /**
     * Marca uma lição como concluída para um usuário.
     * Após a conclusão, verifica se o curso inteiro foi finalizado.
     *
     * @param userId   o ID do usuário
     * @param lessonId o ID da lição a ser marcada como concluída
     * @return O registro do progresso criado
     */
    @Transactional
    public LessonProgress markLessonAsCompleted(UUID userId, UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lição não encontrada com o ID: " + lessonId));

        Course course = lesson.getModule().getCourse();

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, course.getId())
                .orElseThrow(() -> new IllegalStateException("Usuário com ID " + userId + " não está matriculado no curso."));

        // Evita duplicar o registro de progresso
        if (lessonProgressRepository.existsByEnrollmentAndLesson(enrollment, lesson)) {
            // Pode-se retornar o progresso existente ou uma exceção, dependendo da regra de negócio.
            // Aqui, simplesmente evitamos a duplicação.
            throw new IllegalStateException("Esta lição já foi marcada como concluída.");
        }

        LessonProgress progress = LessonProgress.builder()
                .enrollment(enrollment)
                .lesson(lesson)
                .completedAt(OffsetDateTime.now())
                .build();

        LessonProgress savedProgress = lessonProgressRepository.save(progress);

        // Após salvar o progresso, verifica a conclusão do curso
        checkCourseCompletion(enrollment);

        return savedProgress;
    }

    /**
     * Verifica se todas as lições de um curso foram concluídas para uma determinada matrícula.
     * Se sim, o status da matrícula é atualizado para COMPLETED.
     *
     * @param enrollment A matrícula a ser verificada
     */
    private void checkCourseCompletion(Enrollment enrollment) {
        Course course = enrollment.getCourse();

        // 1. Conta o total de lições no curso de forma otimizada
        long totalLessonsInCourse = lessonRepository.countByCourseId(course.getId());

        // 2. Conta o total de lições concluídas para esta matrícula
        long completedLessonsForEnrollment = lessonProgressRepository.countByEnrollment(enrollment);

        // 3. Compara e atualiza o status se necessário
        if (totalLessonsInCourse > 0 && totalLessonsInCourse == completedLessonsForEnrollment) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollmentRepository.save(enrollment);
        }
    }
}