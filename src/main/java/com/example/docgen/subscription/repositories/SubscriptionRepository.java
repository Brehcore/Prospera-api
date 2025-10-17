package com.example.docgen.subscription.repositories;

import com.example.docgen.enterprise.domain.Account;
import com.example.docgen.subscription.entities.Subscription;
import com.example.docgen.subscription.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    // --- NOVOS MÉTODOS (baseados em Account) ---

    /**
     * Busca uma assinatura ativa para uma Conta específica.
     */
    Optional<Subscription> findByAccountAndStatus(Account account, SubscriptionStatus status);

    /**
     * Verifica se uma Conta específica já possui uma assinatura ativa.
     */
    boolean existsByAccountAndStatus(Account account, SubscriptionStatus status);


    /**
     * QUERY DO PAYWALL ATUALIZADA
     * Verifica se um usuário tem acesso a um treinamento através de QUALQUER
     * conta à qual ele esteja vinculado (seja pessoal ou de organização).
     */
    @Query("SELECT COUNT(s) > 0 FROM Subscription s " +
            "JOIN s.plan p " +
            "JOIN p.trainings t " +
            "WHERE s.status = :status " +
            "AND :now BETWEEN s.startDate AND s.endDate " +
            "AND t.id = :trainingId " +
            "AND ( " +
            "  s.account.id = (SELECT u.personalAccount.id FROM AuthUser u WHERE u.id = :userId) OR " +
            "  s.account.id IN (SELECT m.organization.account.id FROM Membership m WHERE m.user.id = :userId) " +
            ")")
    boolean doesUserHaveActiveSubscriptionForTraining(
            @Param("userId") UUID userId,
            @Param("trainingId") UUID trainingId,
            @Param("status") SubscriptionStatus status,
            @Param("now") OffsetDateTime now
    );

}