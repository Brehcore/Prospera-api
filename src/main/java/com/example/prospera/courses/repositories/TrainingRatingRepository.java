package com.example.prospera.courses.repositories;

import com.example.prospera.courses.domain.TrainingRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TrainingRatingRepository extends JpaRepository<TrainingRating, UUID> {
    // Para verificar se já existe avaliação para essa matrícula
    boolean existsByEnrollmentId(UUID enrollmentId);

    Optional<TrainingRating> findByEnrollmentId(UUID id);
}