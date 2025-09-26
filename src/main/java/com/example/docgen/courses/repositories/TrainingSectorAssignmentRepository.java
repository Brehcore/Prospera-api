package com.example.docgen.courses.repositories;

import com.example.docgen.courses.domain.TrainingSectorAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface TrainingSectorAssignmentRepository extends JpaRepository<TrainingSectorAssignment, UUID> {

    List<TrainingSectorAssignment> findBySectorIdIn(List<UUID> sectorIds);

    /**
     * Encontra todas as associações de treinamento para um ID de setor.
     */
    List<TrainingSectorAssignment> findBySectorId(UUID sectorId);

    /**
     * Verifica se existe alguma associação de setor para um treinamento.
     */
    boolean existsByTrainingId(UUID trainingId);

    /**
     * Deleta a associação baseada no ID do treinamento e no ID do setor.
     */
    @Transactional
    void deleteByTrainingIdAndSectorId(UUID trainingId, UUID sectorId);

    /**
     * Verifica se existe alguma associação para um dado sectorId.
     */
    boolean existsBySectorId(UUID sectorId);

}
