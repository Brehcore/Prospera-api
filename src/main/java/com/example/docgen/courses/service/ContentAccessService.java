package com.example.docgen.courses.service;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.courses.domain.EbookTraining;
import com.example.docgen.courses.domain.Training;
import com.example.docgen.courses.repositories.EnrollmentRepository;
import com.example.docgen.courses.repositories.TrainingRepository;
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
    // Futuramente, injetar aqui o seu SubscriptionService

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
        // LÓGICA DE NEGÓCIO (O "PAYWALL"):
        // 1. O usuário está matriculado neste treinamento?
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndTrainingId(user.getId(), trainingId);

        // 2. Futuramente, você adicionaria: OU o usuário tem uma assinatura ativa?
        // boolean hasActiveSubscription = subscriptionService.userHasAccess(user.getId(), trainingId);

        if (!isEnrolled /* && !hasActiveSubscription */) {
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
}