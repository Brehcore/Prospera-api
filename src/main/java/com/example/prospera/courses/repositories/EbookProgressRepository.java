package com.example.prospera.courses.repositories;

import com.example.prospera.courses.domain.EbookProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EbookProgressRepository extends JpaRepository<EbookProgress, UUID> {
    Optional<EbookProgress> findByUserIdAndTrainingId(UUID userId, UUID trainingId);
}