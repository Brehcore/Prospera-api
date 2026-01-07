package com.example.prospera.courses.repositories;

import com.example.prospera.courses.domain.Training;
import com.example.prospera.courses.domain.enums.PublicationStatus;
import com.example.prospera.courses.domain.enums.TrainingEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrainingRepository extends JpaRepository<Training, UUID> {

    /**
     * Encontra todos os cursos disponíveis para uma organização.
     * Inclui cursos públicos (sem organizationId) e os cursos da própria organização.
     */
    @Query("SELECT c FROM Training c WHERE (c.organizationId IS NULL OR c.organizationId = :orgId) AND c.status = 'PUBLISHED'")
    List<Training> findAvailableForOrganization(@Param("orgId") UUID orgId);

    Optional<Training> findByTitle(String title);

    // CORREÇÃO: Usando o operador TREAT para acessar o campo 'modules' da subclasse RecordedCourse.
    @Query("SELECT t FROM Training t LEFT JOIN FETCH TREAT(t AS RecordedCourse).modules m LEFT JOIN FETCH m.lessons WHERE t.id = :id")
    Optional<Training> findByIdWithModulesAndLessons(@Param("id") UUID id);

    // Retorna treinamentos que não têm nenhuma associação na tabela training_sector_assignments
    @Query("SELECT t FROM Training t WHERE t.status = 'PUBLISHED' AND NOT EXISTS " +
            "(SELECT 1 FROM TrainingSectorAssignment tsa WHERE tsa.trainingId = t.id)")
    List<Training> findUniversalPublishedTrainings();

    List<Training> findByEntityType(TrainingEntityType entityType);

    /**
     * NOVO MÉTODO: Encontra todos os treinamentos cujo ID está em uma lista fornecida
     * E cujo status de publicação corresponde ao status fornecido.
     */
    List<Training> findAllByIdInAndStatus(List<UUID> ids, PublicationStatus status);


    /**
     * Encontra todos os treinamentos com o status de publicação especificado.
     */
    List<Training> findByStatus(PublicationStatus status);

    /**
     * Encontra um treinamento específico pelo ID e status de publicação.
     */
    Optional<Training> findByIdAndStatus(UUID id, PublicationStatus status);
}
