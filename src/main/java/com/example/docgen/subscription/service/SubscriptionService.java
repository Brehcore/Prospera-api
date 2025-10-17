package com.example.docgen.subscription.service;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.exceptions.BusinessRuleException;
import com.example.docgen.auth.exceptions.ResourceNotFoundException;
import com.example.docgen.auth.repositories.AuthUserRepository;
import com.example.docgen.common.enums.OrganizationRole;
import com.example.docgen.enterprise.domain.Account;
import com.example.docgen.enterprise.domain.Organization;
import com.example.docgen.enterprise.domain.enums.OrganizationStatus;
import com.example.docgen.enterprise.repositories.OrganizationRepository;
import com.example.docgen.enterprise.service.AccountService;
import com.example.docgen.subscription.dto.AccessStatusDTO;
import com.example.docgen.subscription.entities.Plan;
import com.example.docgen.subscription.entities.Subscription;
import com.example.docgen.subscription.enums.AccessType;
import com.example.docgen.subscription.enums.PlanType;
import com.example.docgen.subscription.enums.SubscriptionOrigin;
import com.example.docgen.subscription.enums.SubscriptionStatus;
import com.example.docgen.subscription.repositories.PlanRepository;
import com.example.docgen.subscription.repositories.SubscriptionRepository;
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

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AuthUserRepository authUserRepository;
    private final OrganizationRepository organizationRepository;
    private final AccountService accountService;

    // =========================================================================
    // MÉTODOS DE CRIAÇÃO
    // =========================================================================

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

    // =========================================================================
    // LÓGICA DE VERIFICAÇÃO DE ACESSO
    // =========================================================================

    @Transactional(readOnly = true)
    public AccessStatusDTO getAccessStatusForUser(AuthUser currentUser) {
        if (currentUser.getMemberships() != null && !currentUser.getMemberships().isEmpty()) {
            Optional<AccessStatusDTO> orgAccessStatus = currentUser.getMemberships().stream()
                    .map(membership -> membership.getOrganization().getAccount())
                    .filter(Objects::nonNull).distinct()
                    .flatMap(account -> subscriptionRepository.findByAccountAndStatus(account, SubscriptionStatus.ACTIVE).stream())
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
        if (currentUser.getPersonalAccount() != null) {
            Optional<Subscription> personalSub = subscriptionRepository.findByAccountAndStatus(currentUser.getPersonalAccount(), SubscriptionStatus.ACTIVE);
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

    // =========================================================================
    // OUTROS MÉTODOS DE GESTÃO
    // =========================================================================

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

    // =========================================================================
    // METODO PRIVADO AUXILIAR
    // =========================================================================

    private Subscription createSubscriptionForAccount(Account account, Plan plan) {
        Subscription newSubscription = new Subscription();
        newSubscription.setAccount(account);
        newSubscription.setPlan(plan);
        newSubscription.setStatus(SubscriptionStatus.ACTIVE);
        newSubscription.setOrigin(SubscriptionOrigin.MANUAL);
        OffsetDateTime startDate = OffsetDateTime.now();
        newSubscription.setStartDate(startDate);
        newSubscription.setEndDate(startDate.plusDays(plan.getDurationInDays()));
        // CORREÇÃO: Faltava salvar a entidade.
        return subscriptionRepository.save(newSubscription);
    }
}