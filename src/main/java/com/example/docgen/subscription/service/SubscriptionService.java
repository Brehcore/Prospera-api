package com.example.docgen.subscription.service;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.exceptions.BusinessRuleException;
import com.example.docgen.auth.exceptions.ResourceNotFoundException;
import com.example.docgen.auth.repositories.AuthUserRepository;
import com.example.docgen.common.enums.OrganizationRole;
import com.example.docgen.enterprise.domain.Membership;
import com.example.docgen.enterprise.domain.Organization;
import com.example.docgen.enterprise.domain.enums.OrganizationStatus;
import com.example.docgen.enterprise.repositories.MembershipRepository;
import com.example.docgen.enterprise.repositories.OrganizationRepository;
import com.example.docgen.subscription.entities.Plan;
import com.example.docgen.subscription.entities.Subscription;
import com.example.docgen.subscription.enums.SubscriptionOrigin;
import com.example.docgen.subscription.enums.SubscriptionStatus;
import com.example.docgen.subscription.repositories.PlanRepository;
import com.example.docgen.subscription.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TODO: Quando  implementar a integração com o gateway de pagamento no futuro, a lógica de lá setará a origem como PAYMENT_GATEWAY
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AuthUserRepository authUserRepository;
    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public Subscription createSubscriptionForUser(UUID userId, UUID planId) {
        // Busca o Plano
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano com ID " + planId + " não encontrado."));

        // Busca o Usuário
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + userId + " não encontrado."));

        // Plano está ativo?
        if (!plan.isActive()) {
            throw new BusinessRuleException("O plano " + plan.getName() + " não está mais ativo.");
        }

        // Verifica se o usuário é um ORG_MEMBER em QUALQUER organização que esteja ATIVA.
        if (user.getMemberships() != null && !user.getMemberships().isEmpty()) {

            boolean isBlockedMember = user.getMemberships().stream()
                    // A condição agora verifica duas coisas:
                    .anyMatch(membership ->
                            // 1. A organização está ativa? E...
                            membership.getOrganization().getStatus() == OrganizationStatus.ACTIVE &&
                                    // 2. O papel do usuário nesta organização é de MEMBRO?
                                    membership.getRole() == OrganizationRole.ORG_MEMBER
                    );

            if (isBlockedMember) {
                throw new BusinessRuleException(
                        "Membros de uma organização ativa não podem adquirir assinaturas individuais. Apenas administradores."
                );
            }
        }

        // Usuário já tem uma assinatura pessoal ativa?
        boolean alreadyHasActiveSubscription = subscriptionRepository.existsByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE);
        if (alreadyHasActiveSubscription) {
            throw new BusinessRuleException("O usuário já possui uma assinatura ativa.");
        }

        // Se todas as validações passaram, cria e salva a nova assinatura.
        Subscription newSubscription = new Subscription();
        newSubscription.setPlan(plan);
        newSubscription.setUser(user);
        newSubscription.setStatus(SubscriptionStatus.ACTIVE);
        newSubscription.setOrigin(SubscriptionOrigin.MANUAL);

        OffsetDateTime startDate = OffsetDateTime.now();
        newSubscription.setStartDate(startDate);
        newSubscription.setEndDate(startDate.plusDays(plan.getDurationInDays()));

        return subscriptionRepository.save(newSubscription);
    }

    /**
     * Encontra a assinatura ativa para um usuário específico.
     *
     * @param currentUser O objeto do usuário autenticado.
     * @return A entidade Subscription encontrada.
     * @throws ResourceNotFoundException se nenhuma assinatura ativa for encontrada.
     */
    public Subscription findActiveSubscriptionForUser(AuthUser currentUser) {
        return subscriptionRepository.findByUserAndStatus(currentUser, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma assinatura ativa encontrada para o usuário."));
    }

    @Transactional(readOnly = true)
    public List<Subscription> findAllSubscriptionsByOrganization(UUID orgId, AuthUser currentUser) {
        // 1. VERIFICAÇÃO DE SEGURANÇA (permanece a mesma)
        boolean isAdminOfThisOrg = currentUser.getMemberships().stream()
                .anyMatch(membership -> membership.getOrganization().getId().equals(orgId) &&
                        membership.getRole() == OrganizationRole.ORG_ADMIN);

        if (!isAdminOfThisOrg) {
            throw new AccessDeniedException("Acesso negado. Você não é administrador desta organização.");
        }

        // 2. BUSCAR A ENTIDADE DA ORGANIZAÇÃO (Passo novo)
        // Precisamos do objeto Organization para usar seu método de busca otimizado.
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organização com ID " + orgId + " não encontrada."));


        // 3. BUSCAR TODOS OS MEMBROS USANDO SEU MÉTODO OTIMIZADO
        // Usamos o seu método existente que já é performático.
        List<Membership> memberships = membershipRepository.findByOrganizationWithDetails(organization);

        // Extrai a lista de AuthUser de cada filiação.
        List<AuthUser> members = memberships.stream()
                .map(Membership::getUser)
                .toList();

        if (members.isEmpty()) {
            return List.of();
        }

        // 4. BUSCAR AS ASSINATURAS (permanece o mesmo)
        return subscriptionRepository.findByUserIn(members);
    }

    public boolean hasActiveSubscriptionForTraining(UUID userId, UUID trainingId) {
        return subscriptionRepository.doesUserHaveActiveSubscriptionForTraining(
                userId,
                trainingId,
                SubscriptionStatus.ACTIVE,
                OffsetDateTime.now()
        );
    }

    // Cancelar assinatura
    @Transactional
    public void cancelSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Assinatura com ID " + subscriptionId + " não encontrada."));

        if (subscription.getStatus() == SubscriptionStatus.CANCELED) {
            throw new BusinessRuleException("Esta assinatura já está cancelada.");
        }

        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscription.setEndDate(OffsetDateTime.now());
        subscriptionRepository.save(subscription);
    }

    /**
     * Retorna todas as assinaturas do sistema, com filtros opcionais.
     *
     * @param status Filtra por status (ACTIVE, EXPIRED, CANCELED). Opcional.
     * @param origin Filtra por origem (MANUAL, PAYMENT_GATEWAY). Opcional.
     * @return Uma lista de assinaturas.
     */
    @Transactional(readOnly = true)
    public List<Subscription> findAllSubscriptions(SubscriptionStatus status, SubscriptionOrigin origin) {
        List<Subscription> allSubscriptions = subscriptionRepository.findAll();

        // Aplica os filtros se eles foram fornecidos
        if (status != null) {
            allSubscriptions = allSubscriptions.stream()
                    .filter(s -> s.getStatus() == status)
                    .collect(Collectors.toList());
        }
        if (origin != null) {
            allSubscriptions = allSubscriptions.stream()
                    .filter(s -> s.getOrigin() == origin)
                    .collect(Collectors.toList());
        }
        return allSubscriptions;
    }
}
