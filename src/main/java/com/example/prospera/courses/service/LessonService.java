package com.example.prospera.courses.service;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.common.enums.UserRole;
import com.example.prospera.courses.domain.Lesson;
import com.example.prospera.courses.domain.Module;
import com.example.prospera.courses.repositories.EnrollmentRepository;
import com.example.prospera.courses.repositories.LessonProgressRepository;
import com.example.prospera.courses.repositories.LessonRepository;
import com.example.prospera.courses.repositories.ModuleRepository;
import com.example.prospera.exceptions.ResourceNotFoundException;
import com.example.prospera.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;
    private final SubscriptionService subscriptionService;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;

    /**
     * Busca os detalhes da aula para consumo (assistir).
     * Inclui validação de segurança (Paywall).
     */
    @Transactional(readOnly = true)
    public Lesson findLessonForUser(UUID lessonId, AuthUser user) {
        // 1. Busca a aula (ou lança erro 404)
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Aula não encontrada."));

        // 2. Bypass para Administradores (System Admin)
        if (user.getRole() == UserRole.SYSTEM_ADMIN) {
            return lesson;
        }

        // 3. Identifica o curso/treinamento pai
        UUID trainingId = lesson.getModule().getCourse().getId();

        // 4. Verifica Matrícula (Enrollment)
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndTrainingId(user.getId(), trainingId);

        // 5. Verifica Assinatura Ativa (Subscription)
        boolean hasActiveSubscription = subscriptionService.hasActiveSubscriptionForTraining(user.getId(), trainingId);

        // 6. Se não tiver acesso por nenhum meio, bloqueia (403 Forbidden)
        if (!isEnrolled && !hasActiveSubscription) {
            throw new AccessDeniedException("Você não tem permissão para assistir a esta aula.");
        }

        return lesson;
    }

    /**
     * Verifica se a aula está concluída pelo usuário.
     */
    @Transactional(readOnly = true)
    public boolean isLessonCompleted(UUID lessonId, UUID userId) {
        // Precisamos achar a matrícula primeiro para verificar o progresso
        // Uma forma otimizada seria criar um metodo no Repository que faz o JOIN
        // Mas usando o que você já tem:

        var lesson = lessonRepository.findById(lessonId).orElseThrow();
        var trainingId = lesson.getModule().getCourse().getId();

        var enrollment = enrollmentRepository.findByUserIdAndTrainingId(userId, trainingId);

        if (enrollment.isPresent()) {
            return lessonProgressRepository.existsByEnrollmentAndLesson(enrollment.get(), lesson);
        }
        return false;
    }

    @Transactional(readOnly = true)
    public Optional<Lesson> findNextLesson(UUID currentLessonId) {
        Lesson currentLesson = lessonRepository.findById(currentLessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Aula com ID " + currentLessonId + " não encontrada."));

        Module currentModule = currentLesson.getModule();

        Optional<Lesson> nextLessonInModule = lessonRepository.findByModuleAndLessonOrder(currentModule, currentLesson.getLessonOrder() + 1);

        if (nextLessonInModule.isPresent()) {
            return nextLessonInModule;
        }

        Optional<Module> nextModule = moduleRepository.findByCourseAndModuleOrder(
                currentModule.getCourse(),
                currentModule.getModuleOrder() + 1
        );

        return nextModule.flatMap(module -> module.getLessons().stream().findFirst());
    }

    @Transactional(readOnly = true)
    public Optional<Lesson> findPreviousLesson(UUID currentLessonId) {
        Lesson currentLesson = lessonRepository.findById(currentLessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Aula com ID " + currentLessonId + " não encontrada."));

        if (currentLesson.getLessonOrder() > 1) {
            return lessonRepository.findByModuleAndLessonOrder(currentLesson.getModule(), currentLesson.getLessonOrder() - 1);
        }

        Module currentModule = currentLesson.getModule();
        Optional<Module> previousModule = moduleRepository.findByCourseAndModuleOrder(
                currentModule.getCourse(),
                currentModule.getModuleOrder() - 1
        );

        return previousModule.flatMap(module -> {
            List<Lesson> lessons = module.getLessons();
            if (lessons.isEmpty()) {
                // Se o módulo não tiver aulas, não há aula anterior para retornar.
                return Optional.empty();
            }
            return Optional.of(lessons.getLast());
        });
    }
}