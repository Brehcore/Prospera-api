package com.example.docgen.courses.repositories;

import com.example.docgen.courses.domain.EbookProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EbookProgressRepository extends JpaRepository<EbookProgress, UUID> {
    Optional<EbookProgress> findByUserIdAndTrainingId(UUID userId, UUID trainingId);
}