package com.example.prospera.subscription.service;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.auth.repositories.AuthUserRepository;
import com.example.prospera.common.enums.OrganizationRole;
import com.example.prospera.enterprise.domain.Account;
import com.example.prospera.enterprise.domain.Organization;
import com.example.prospera.enterprise.domain.enums.OrganizationStatus;
import com.example.prospera.enterprise.repositories.OrganizationRepository;
import com.example.prospera.enterprise.service.AccountService;
import com.example.prospera.exceptions.BusinessRuleException;
import com.example.prospera.exceptions.ResourceNotFoundException;
import com.example.prospera.subscription.dto.AccessStatusDTO;
import com.example.prospera.subscription.entities.Plan;
import com.example.prospera.subscription.entities.Subscription;
import com.example.prospera.subscription.enums.AccessType;
import com.example.prospera.subscription.enums.PlanType;
import com.example.prospera.subscription.enums.SubscriptionOrigin;
import com.example.prospera.subscription.enums.SubscriptionStatus;
import com.example.prospera.subscription.repositories.PlanRepository;
import com.example.prospera.subscription.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo gerenciamento de assinaturas no sistema.
 * <p>
 * Este serviço oferece funcionalidades para:
 * - Criar assinaturas individuais e organizacionais
 * - Gerenciar o acesso baseado em assinaturas
 * - Verificar status de assinaturas
 * - Cancelar assinaturas
 * - Consultar assinaturas existentes
 * <p>
 * O serviço implementa regras de negócio importantes como:
 * - Validação de tipos de planos para diferentes tipos de assinatura
 * - Verificação de assinaturas ativas duplicadas
 * - Controle de acesso baseado em papéis
 * - Gestão do ciclo de vida das assinaturas
 *
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AuthUserRepository authUserRepository;
    private final OrganizationRepository organizationRepository;
    private final AccountService accountService;

    /**
     * Cria uma assinatura pessoal para um usuário específico.
     *
     * @param userId ID do usuário que receberá a assinatura
     * @param planId ID do plano a ser assinado
     * @return Nova assinatura criada
     * @throws ResourceNotFoundException se o usuário ou plano não forem encontrados
     * @throws BusinessRuleException     se houver violação das regras de negócio
     */
    @Transactional
    public Subscription createPersonalSubscription(UUID userId, UUID planId) {
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado."));
        AuthUser user = authUserRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (plan.getType() != PlanType.INDIVIDUAL) {
            throw new BusinessRuleException("Apenas planos do tipo INDIVIDUAL podem ser assinados pessoalmente.");
        }
        if (user.getMemberships() != null && !user.getMemberships().isEmpty()) {
            boolean isBlockedMember = user.getMemberships().stream()
                    .anyMatch(m -> m.getOrganization().getStatus() == OrganizationStatus.ACTIVE && m.getRole() == OrganizationRole.ORG_MEMBER);
            if (isBlockedMember) {
                throw new BusinessRuleException("Membros de organizações ativas não podem adquirir assinaturas individuais.");
            }
        }

        Account personalAccount = accountService.findOrCreatePersonalAccountForUser(user);
        if (subscriptionRepository.existsByAccountAndStatus(personalAccount, SubscriptionStatus.ACTIVE)) {
            throw new BusinessRuleException("Este usuário já possui uma assinatura individual ativa.");
        }

        return createSubscriptionForAccount(personalAccount, plan);
    }

    /**
     * Cria uma assinatura para uma organização.
     *
     * @param organizationId ID da organização que receberá a assinatura
     * @param planId         ID do plano a ser assinado
     * @return Nova assinatura criada
     * @throws ResourceNotFoundException se a organização ou plano não forem encontrados
     * @throws BusinessRuleException     se houver violação das regras de negócio
     */
    @Transactional
    public Subscription createSubscriptionForOrganization(UUID organizationId, UUID planId) {
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado."));
        if (plan.getType() != PlanType.ENTERPRISE) {
            throw new BusinessRuleException("Apenas planos do tipo ENTERPRISE podem ser atribuídos a organizações.");
        }
        Organization organization = organizationRepository.findById(organizationId).orElseThrow(() -> new ResourceNotFoundException("Organização não encontrada."));
        Account account = organization.getAccount();
        if (account == null) {
            throw new IllegalStateException("A organização não está associada a uma conta.");
        }
        if (subscriptionRepository.existsByAccountAndStatus(account, SubscriptionStatus.ACTIVE)) {
            throw new BusinessRuleException("Esta conta (e suas organizações) já possui uma assinatura ativa.");
        }
        return createSubscriptionForAccount(account, plan);
    }

    /**
     * Verifica e retorna o status de acesso atual de um usuário.
     * Considera tanto assinaturas organizacionais quanto pessoais.
     *
     * @param currentUser Usuário atual do sistema
     * @return DTO contendo informações sobre o status de acesso do usuário
     */
    @Transactional(readOnly = true)
    public AccessStatusDTO getAccessStatusForUser(AuthUser currentUser) {

        AuthUser user = authUserRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        OffsetDateTime now = OffsetDateTime.now(); // Captura o momento atual

        if (user.getMemberships() != null && !user.getMemberships().isEmpty()) {
            Optional<AccessStatusDTO> orgAccessStatus = user.getMemberships().stream()
                    .map(membership -> membership.getOrganization().getAccount())
                    .filter(Objects::nonNull).distinct()
                    .flatMap(account -> subscriptionRepository.findByAccountAndStatusAndEndDateAfter(account, SubscriptionStatus.ACTIVE, now).stream())
                    .findFirst()
                    .map(activeSub -> new AccessStatusDTO(
                            AccessType.ORGANIZATIONAL_SUBSCRIPTION,
                            activeSub.getPlan().getName(),
                            activeSub.getEndDate(),
                            activeSub.getAccount().getName()
                    ));
            if (orgAccessStatus.isPresent()) {
                return orgAccessStatus.get();
            }
        }

        // Use 'user' aqui também
        if (user.getPersonalAccount() != null) {
            Optional<Subscription> personalSub = subscriptionRepository.findByAccountAndStatusAndEndDateAfter(user.getPersonalAccount(), SubscriptionStatus.ACTIVE, now);
            if (personalSub.isPresent()) {
                Subscription sub = personalSub.get();
                return new AccessStatusDTO(AccessType.PERSONAL_SUBSCRIPTION, sub.getPlan().getName(), sub.getEndDate(), null);
            }
        }
        return new AccessStatusDTO(AccessType.NONE, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<Subscription> findAllSubscriptionsByOrganization(UUID orgId, AuthUser currentUser) {
        boolean isAdminOfThisOrg = currentUser.getMemberships().stream()
                .anyMatch(m -> m.getOrganization().getId().equals(orgId) && m.getRole() == OrganizationRole.ORG_ADMIN);
        if (!isAdminOfThisOrg) {
            throw new AccessDeniedException("Acesso negado.");
        }
        Organization organization = organizationRepository.findById(orgId).orElseThrow(() -> new ResourceNotFoundException("Organização não encontrada."));
        Account account = organization.getAccount();
        if (account == null) return Collections.emptyList();

        // CORREÇÃO: Busca a assinatura da CONTA e retorna como uma lista.
        return subscriptionRepository.findByAccountAndStatus(account, SubscriptionStatus.ACTIVE)
                .stream().toList();
    }

    public boolean hasActiveSubscriptionForTraining(UUID userId, UUID trainingId) {
        // CORREÇÃO: A query no repositório precisa ser atualizada para o modelo de Account.
        // Assumindo que a query foi corrigida no SubscriptionRepository.
        return subscriptionRepository.doesUserHaveActiveSubscriptionForTraining(
                userId, trainingId, SubscriptionStatus.ACTIVE, OffsetDateTime.now());
    }

    @Transactional
    public void cancelSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(() -> new ResourceNotFoundException("Assinatura não encontrada."));
        if (subscription.getStatus() == SubscriptionStatus.CANCELED) {
            throw new BusinessRuleException("Esta assinatura já está cancelada.");
        }
        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscription.setEndDate(OffsetDateTime.now());
        subscriptionRepository.save(subscription);
    }

    @Transactional(readOnly = true)
    public List<Subscription> findAllSubscriptions(SubscriptionStatus status, SubscriptionOrigin origin) {
        List<Subscription> allSubscriptions = subscriptionRepository.findAll();
        if (status != null) {
            allSubscriptions = allSubscriptions.stream().filter(s -> s.getStatus() == status).collect(Collectors.toList());
        }
        if (origin != null) {
            allSubscriptions = allSubscriptions.stream().filter(s -> s.getOrigin() == origin).collect(Collectors.toList());
        }
        return allSubscriptions;
    }


    /**
     * Rotina executada periodicamente para verificar e expirar assinaturas vencidas.
     * Requer @EnableScheduling na classe Application principal.
     */
    // Executa a cada hora (ajuste a cron expression conforme necessário)
    // Exemplo: "0 0 * * * *" = toda hora cheia
    // Exemplo para teste (a cada minuto): "0 * * * * *"
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireOverdueSubscriptions() {
        OffsetDateTime now = OffsetDateTime.now();

        // Busca assinaturas que estão marcadas como ACTIVE mas já passaram da data
        List<Subscription> overdueSubscriptions = subscriptionRepository
                .findAllByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, now);

        if (!overdueSubscriptions.isEmpty()) {
            System.out.println("Encontradas " + overdueSubscriptions.size() + " assinaturas vencidas. Processando...");

            overdueSubscriptions.forEach(sub -> {
                // Certifique-se de ter o status EXPIRED no seu enum, ou use INACTIVE/CANCELED
                sub.setStatus(SubscriptionStatus.EXPIRED);
            });

            subscriptionRepository.saveAll(overdueSubscriptions);
            System.out.println("Assinaturas atualizadas com sucesso.");
        }
    }

    private Subscription createSubscriptionForAccount(Account account, Plan plan) {
        Subscription newSubscription = new Subscription();
        newSubscription.setAccount(account);
        newSubscription.setPlan(plan);
        newSubscription.setStatus(SubscriptionStatus.ACTIVE);
        newSubscription.setOrigin(SubscriptionOrigin.MANUAL);
        OffsetDateTime startDate = OffsetDateTime.now();
        newSubscription.setStartDate(startDate);
        newSubscription.setEndDate(startDate.plusDays(plan.getDurationInDays()));
        return subscriptionRepository.save(newSubscription);
    }
}