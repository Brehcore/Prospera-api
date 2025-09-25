package com.example.docgen.courses.repositories;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.courses.domain.Enrollment;
import com.example.docgen.courses.domain.Training;
import com.example.docgen.enterprise.domain.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    boolean existsByUserAndTraining(AuthUser user, Training training);

    Optional<Enrollment> findByUserIdAndTrainingId(UUID userId, UUID trainingId);

    List<Enrollment> findByUserIdAndTrainingIdIn(UUID userId, Set<UUID> trainingIds);


    /**
     * Busca, de uma só vez, todos os IDs de usuários de uma lista que já estão
     * matriculados em um treinamento específico.
     * @param trainingId O ID do treinamento em questão.
     * @param userIds A lista de IDs de usuários para verificar.
     * @return Um Set com os IDs dos usuários que já possuem matrícula.
     */
    @Query("SELECT e.user.id FROM Enrollment e WHERE e.training.id = :trainingId AND e.user.id IN :userIds")
    Set<UUID> findEnrolledUserIdsByTrainingAndUserIds(@Param("trainingId") UUID trainingId, @Param("userIds") List<UUID> userIds);

    /**
     * Retorna as afiliações (Memberships) de todos os usuários que estão
     * matriculados em um treinamento específico (trainingId) E que também
     * pertencem a uma organização específica (organizationId).
     */
    @Query("SELECT m FROM Membership m " +
            "JOIN m.user u " +
            "JOIN Enrollment e ON e.user = u " +
            "WHERE m.organization.id = :organizationId AND e.training.id = :trainingId")
    List<Membership> findMembershipsByOrganizationAndTraining(
            @Param("organizationId") UUID organizationId,
            @Param("trainingId") UUID trainingId);

    /**
     * Busca todas as matrículas de um usuário e, na mesma consulta (JOIN FETCH),
     * já carrega os dados do treinamento associado para evitar N+1 queries.
     */
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.training WHERE e.user = :user")
    List<Enrollment> findByUserWithTrainingDetails(@Param("user") AuthUser user);

    /**
     * Verifica se existe alguma matrícula para um treinamento específico.
     */
    boolean existsByTrainingId(UUID trainingId);
}