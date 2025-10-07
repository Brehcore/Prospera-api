package com.example.docgen.subscription.repositories;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.subscription.entities.Subscription;
import com.example.docgen.subscription.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    // Este método vai gerar um SQL para verificar se existe um registro
    // que bate com o userId E com o status fornecido. Retorna true ou false.
    boolean existsByUserIdAndStatus(UUID userId, SubscriptionStatus status);

    /**
     * Busca uma assinatura que pertença a um usuário específico e que tenha um status específico (no caso, ACTIVE)
     */
    Optional<Subscription> findByUserAndStatus(AuthUser user, SubscriptionStatus status);


    /**
     * Busca todas as assinaturas que pertencem a uma lista de usuários
     */
    List<Subscription> findByUserIn(List<AuthUser> users);

    @Query("SELECT COUNT(s) > 0 FROM Subscription s " +
            "JOIN s.plan p " +
            "JOIN p.trainings t " + // Assumindo que a relação em Plan se chama 'trainings'
            "WHERE s.user.id = :userId " +
            "AND s.status = :status " +
            "AND :now BETWEEN s.startDate AND s.endDate " +
            "AND t.id = :trainingId")
    boolean doesUserHaveActiveSubscriptionForTraining(
            @Param("userId") UUID userId,
            @Param("trainingId") UUID trainingId,
            @Param("status") SubscriptionStatus status,
            @Param("now") OffsetDateTime now
    );


}
