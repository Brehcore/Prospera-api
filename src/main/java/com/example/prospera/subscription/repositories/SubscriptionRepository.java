package com.example.prospera.subscription.repositories;

import com.example.prospera.enterprise.domain.Account;
import com.example.prospera.subscription.entities.Subscription;
import com.example.prospera.subscription.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
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

    /**
     * Busca todas as assinaturas que possuem um status específico (ex: ACTIVE)
     * MAS cuja data de término (endDate) é anterior à data informada (ex: agora).
     * Usado para a rotina de expiração automática.
     */
    List<Subscription> findAllByStatusAndEndDateBefore(SubscriptionStatus status, OffsetDateTime date);

    /**
     * Busca assinatura ativa validando também se a data ainda é válida.
     * Isso previne retornar "sujeira" do banco caso o Job de limpeza falhe.
     */
    Optional<Subscription> findByAccountAndStatusAndEndDateAfter(Account account, SubscriptionStatus status, OffsetDateTime now);

}