package com.example.prospera.courses.service;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.common.events.TrainingRatedEvent;
import com.example.prospera.courses.domain.Enrollment;
import com.example.prospera.courses.domain.TrainingRating;
import com.example.prospera.courses.dto.RatingRequestDTO;
import com.example.prospera.courses.repositories.EnrollmentRepository;
import com.example.prospera.courses.repositories.TrainingRatingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrainingRatingService {

    private final TrainingRatingRepository ratingRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void rateTraining(AuthUser user, UUID trainingId, RatingRequestDTO dto) {
        // 1. Busca a matrícula
        Enrollment enrollment = enrollmentRepository.findByUserIdAndTrainingId(user.getId(), trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Você não está matriculado neste treinamento."));

        // 2. Verifica duplicidade
        if (ratingRepository.existsByEnrollmentId(enrollment.getId())) {
            throw new IllegalStateException("Você já avaliou este treinamento.");
        }

        // 3. Salva a avaliação
        TrainingRating rating = TrainingRating.builder()
                .enrollment(enrollment)
                .score(dto.score())
                .comment(dto.comment())
                .build();

        TrainingRating savedRating = ratingRepository.save(rating);

        // 4. Dispara o Evento para o Analytics (Assíncrono)
        TrainingRatedEvent event = new TrainingRatedEvent(
                trainingId,
                user.getId(),
                dto.score(),
                dto.comment(),
                savedRating.getRatedAt()
        );

        eventPublisher.publishEvent(event);
    }
}