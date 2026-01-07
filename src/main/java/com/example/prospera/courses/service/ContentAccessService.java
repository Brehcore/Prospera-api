package com.example.prospera.courses.service;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.common.enums.UserRole;
import com.example.prospera.courses.domain.EbookTraining;
import com.example.prospera.courses.domain.Training;
import com.example.prospera.courses.repositories.EnrollmentRepository;
import com.example.prospera.courses.repositories.TrainingRepository;
import com.example.prospera.subscription.service.SubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentAccessService {

    private final FileStorageService fileStorageService;
    private final TrainingRepository trainingRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubscriptionService subscriptionService;

    /**
     * Carrega um recurso de imagem pública. Não há verificação de segurança aqui.
     */
    public Resource loadImageResource(String filename) {
        return fileStorageService.loadAsResource(filename);
    }

    /**
     * Carrega o recurso de um e-book, mas apenas se o usuário tiver permissão.
     */
    public Resource loadEbookForUser(AuthUser user, UUID trainingId) {

        // --- LÓGICA DE BYPASS PARA O SYSTEM_ADMIN ---
        // 3. Verifica se a role do usuário é SYSTEM_ADMIN.
        if (user.getRole() == UserRole.SYSTEM_ADMIN) {
            // Se for, concede o acesso imediatamente e pula todas as outras verificações.
            return loadTrainingResource(trainingId);
        }

        // LÓGICA DE NEGÓCIO (O "PAYWALL"):
        // O usuário está matriculado neste treinamento?
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndTrainingId(user.getId(), trainingId);

        boolean hasActiveSubscription = subscriptionService.hasActiveSubscriptionForTraining(user.getId(), trainingId);

        if (!isEnrolled && !hasActiveSubscription) {
            throw new AccessDeniedException("Você não tem permissão para acessar este conteúdo.");
        }

        // Se o acesso for permitido, busca o nome do arquivo e o carrega.
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado."));

        if (!(training instanceof EbookTraining ebook)) {
            throw new IllegalArgumentException("O conteúdo solicitado não é um e-book.");
        }

        return fileStorageService.loadAsResource(ebook.getFilePath());
    }

    /**
     * Método privado para evitar duplicação de código.
     * Busca o treinamento e carrega o recurso do arquivo.
     */
    private Resource loadTrainingResource(UUID trainingId) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado."));

        if (!(training instanceof EbookTraining ebook)) {
            throw new IllegalArgumentException("O conteúdo solicitado não é um e-book.");
        }

        if (ebook.getFilePath() == null || ebook.getFilePath().isBlank()) {
            throw new EntityNotFoundException("O arquivo deste e-book ainda não foi enviado.");
        }

        return fileStorageService.loadAsResource(ebook.getFilePath());
    }
}