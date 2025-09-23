package com.example.docgen.courses.repositories;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.courses.domain.Enrollment;
import com.example.docgen.courses.domain.Training; // Import necessário
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List; // Import necessário
import java.util.Optional;
import java.util.Set; // Import necessário
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    boolean existsByUserAndTraining(AuthUser user, Training training);

    Optional<Enrollment> findByUserIdAndTrainingId(UUID userId, UUID trainingId);

    List<Enrollment> findByUserIdAndTrainingIdIn(UUID userId, Set<UUID> trainingIds);


    /**
     * Busca, de uma só vez, todos os IDs de usuários de uma lista que já estão
     * matriculados em um treinamento específico.
     *
     * @param trainingId O ID do treinamento em questão.
     * @param userIds A lista de IDs de usuários para verificar.
     * @return Um Set com os IDs dos usuários que já possuem matrícula.
     */
    @Query("SELECT e.user.id FROM Enrollment e WHERE e.training.id = :trainingId AND e.user.id IN :userIds")
    Set<UUID> findEnrolledUserIdsByTrainingAndUserIds(@Param("trainingId") UUID trainingId, @Param("userIds") List<UUID> userIds);

}