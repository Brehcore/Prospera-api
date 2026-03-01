package com.example.prospera.courses.service;

import com.example.prospera.courses.domain.EbookProgress;
import com.example.prospera.courses.domain.EbookTraining;
import com.example.prospera.courses.domain.Enrollment;
import com.example.prospera.courses.domain.Lesson;
import com.example.prospera.courses.domain.LessonProgress;
import com.example.prospera.courses.domain.Training;
import com.example.prospera.courses.domain.enums.EnrollmentStatus;
import com.example.prospera.courses.domain.enums.TrainingEntityType;
import com.example.prospera.courses.dto.EbookProgressDTO;
import com.example.prospera.courses.repositories.EbookProgressRepository;
import com.example.prospera.courses.repositories.EnrollmentRepository;
import com.example.prospera.courses.repositories.LessonProgressRepository;
import com.example.prospera.courses.repositories.LessonRepository;
import com.example.prospera.courses.repositories.TrainingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Serviço responsável por gerenciar o progresso dos usuários em diferentes tipos de treinamentos,
 * como cursos gravados e e-books. Oferece funcionalidades para marcar lições como concluídas,
 * atualizar progresso de leitura de e-books e calcular percentuais de conclusão.
 */
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final EbookProgressRepository ebookProgressRepository;
    private final TrainingRepository trainingRepository;

    /**
     * Marca uma lição específica como concluída para um determinado usuário.
     *
     * @param userId   ID do usuário que completou a lição
     * @param lessonId ID da lição que foi completada
     * @return O registro de progresso da lição criado
     * @throws EntityNotFoundException se a lição não for encontrada
     * @throws IllegalStateException   se o usuário não estiver matriculado ou a lição já estiver concluída
     */
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

    /**
     * Atualiza o progresso de leitura de um e-book para um usuário específico.
     *
     * @param userId       ID do usuário que está lendo o e-book
     * @param trainingId   ID do treinamento (e-book)
     * @param lastPageRead Última página lida pelo usuário
     * @throws EntityNotFoundException  se o treinamento não for encontrado
     * @throws IllegalArgumentException se o treinamento não for um e-book ou a página for inválida
     * @throws IllegalStateException    se o total de páginas do e-book não estiver definido
     */
    @Transactional
    public void updateEbookProgress(UUID userId, UUID trainingId, int lastPageRead) {
        // Valida se o treinamento existe e é um Ebook
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado: " + trainingId));
        if (!(training instanceof EbookTraining)) {
            throw new IllegalArgumentException("Progresso só pode ser registrado para Ebooks.");
        }
        Integer totalPages = ((EbookTraining) training).getTotalPages();

        // Garante que o e-book tem um total de páginas definido
        if (totalPages == null || totalPages == 0) {
            throw new IllegalStateException("Não é possível salvar o progresso pois o total de páginas do e-book não foi definido.");
        }

        // Garante que a página enviada está dentro do intervalo válido (de 0 até o total de páginas)
        if (lastPageRead < 0 || lastPageRead > totalPages) {
            throw new IllegalArgumentException(
                    String.format("Página inválida. O valor deve estar entre 0 e %d.", totalPages)
            );
        }

        EbookProgress progress = ebookProgressRepository.findByUserIdAndTrainingId(userId, trainingId)
                .orElse(EbookProgress.builder().userId(userId).training(training).build());

        progress.setLastPageRead(lastPageRead);
        ebookProgressRepository.save(progress);
    }

    /**
     * Recupera o progresso atual de leitura de um e-book para um usuário específico.
     *
     * @param userId     ID do usuário
     * @param trainingId ID do treinamento (e-book)
     * @return DTO contendo informações sobre o progresso de leitura
     * @throws EntityNotFoundException  se o treinamento não for encontrado
     * @throws IllegalArgumentException se o treinamento não for um e-book
     */
    @Transactional(readOnly = true)
    public EbookProgressDTO getEbookProgress(UUID userId, UUID trainingId) {
        // Busca o treinamento para obter o total de páginas
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado: " + trainingId));

        if (!(training instanceof EbookTraining ebook)) {
            throw new IllegalArgumentException("Progresso só pode ser consultado para Ebooks.");
        }
        Integer totalPages = ebook.getTotalPages();

        // Busca o registro de progresso
        EbookProgress progress = ebookProgressRepository.findByUserIdAndTrainingId(userId, trainingId).orElse(null);

        if (progress == null) {
            return new EbookProgressDTO(0, totalPages, BigDecimal.ZERO, null);
        } else {
            BigDecimal percentage = BigDecimal.ZERO;
            if (totalPages != null && totalPages > 0) {
                percentage = BigDecimal.valueOf(progress.getLastPageRead())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalPages), 2, RoundingMode.HALF_UP);
            }
            return new EbookProgressDTO(progress.getLastPageRead(), totalPages, percentage, progress.getUpdatedAt());
        }
    }


    /**
     * Calcula o percentual de progresso para um curso gravado (RecordedCourse).
     * O cálculo é baseado no número de lições completadas em relação ao total de lições do curso.
     *
     * @param enrollment A matrícula do usuário no curso
     * @return O progresso em percentual (ex: 75.00)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateCourseProgress(Enrollment enrollment) {
        // Garante que a lógica só se aplica a RecordedCourse
        if (enrollment.getTraining().getEntityType() != TrainingEntityType.RECORDED_COURSE) {
            return BigDecimal.ZERO;
        }

        UUID courseId = enrollment.getTraining().getId();

        // 1. Pega o total de aulas do curso
        long totalLessons = lessonRepository.countByCourseId(courseId);

        // Se o curso não tem aulas, o progresso é 0
        if (totalLessons == 0) {
            return BigDecimal.ZERO;
        }

        // 2. Pega o total de aulas que o usuário completou para esta matrícula
        long completedLessons = lessonProgressRepository.countByEnrollment(enrollment);

        // 3. Calcula o percentual usando BigDecimal para precisão
        return BigDecimal.valueOf(completedLessons)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalLessons), 2, RoundingMode.HALF_UP);
    }
}