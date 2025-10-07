package com.example.docgen.subscription.controller;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.subscription.dto.PlanResponse;
import com.example.docgen.subscription.dto.SubscriptionResponse;
import com.example.docgen.subscription.entities.Plan;
import com.example.docgen.subscription.entities.Subscription;
import com.example.docgen.subscription.service.PlanService;
import com.example.docgen.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final PlanService planService;
    private final SubscriptionService subscriptionService;

    /**
     * SubscriptionController, por enquanto, não terá um endpoint para o usuário contratar um plano sozinho.
     * Ele será focado em visualizar as informações
     * O endpoint para o usuário se auto-servir (ex: POST /subscriptions/checkout) será a grande feature da Fase 2,
     * quando for com um sistema de pagamento como Stripe ou Mercado Pago
     */

    @GetMapping("/plans")
    public List<PlanResponse> listAvailablePlans() {
        // Busca a lista de entidades Plan ativas através do serviço.
        List<Plan> activePlans = planService.getActivePlans();

        // Converte (mapeia) a lista de Entidades para uma lista de DTOs.
        return activePlans.stream()
                .map(plan -> new PlanResponse(
                        plan.getId(),
                        plan.getName(),
                        plan.getDescription(),
                        plan.getOriginalPrice(),
                        plan.getCurrentPrice(),
                        plan.getDurationInDays(),
                        plan.isActive()
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/me/subscription")
    @PreAuthorize("isAuthenticated()")
    public SubscriptionResponse getMySubscription(@AuthenticationPrincipal AuthUser currentUser) {
        Subscription activeSubscription = subscriptionService.findActiveSubscriptionForUser(currentUser);

        // Mapeia a entidade para o DTO de resposta
        return new SubscriptionResponse(
                activeSubscription.getId(),
                activeSubscription.getUser().getId(),
                activeSubscription.getPlan().getId(),
                activeSubscription.getPlan().getName(),
                activeSubscription.getStartDate(),
                activeSubscription.getEndDate(),
                activeSubscription.getStatus(),
                activeSubscription.getOrigin()
        );
    }

    /**
     * Endpoint para um ORG_ADMIN ver todas as assinaturas dos membros de sua organização.
     */
    @GetMapping("/organizations/{orgId}/subscriptions")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public List<SubscriptionResponse> getOrganizationSubscriptions(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal AuthUser currentUser) {

        List<Subscription> subscriptions = subscriptionService.findAllSubscriptionsByOrganization(orgId, currentUser);

        return subscriptions.stream()
                .map(sub -> new SubscriptionResponse(
                        sub.getId(),
                        sub.getUser().getId(),
                        sub.getPlan().getId(),
                        sub.getPlan().getName(),
                        sub.getStartDate(),
                        sub.getEndDate(),
                        sub.getStatus(),
                        sub.getOrigin()
                ))
                .toList();
    }
}
