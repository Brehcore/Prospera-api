package com.example.docgen.subscription.controller;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.subscription.dto.AccessStatusDTO;
import com.example.docgen.subscription.dto.PlanResponse;
import com.example.docgen.subscription.entities.Plan;
import com.example.docgen.subscription.enums.PlanType;
import com.example.docgen.subscription.service.PlanService;
import com.example.docgen.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api") // 1. Mapeamento base mais genérico para evitar repetição
@RequiredArgsConstructor
public class SubscriptionController {

    private final PlanService planService;
    private final SubscriptionService subscriptionService;

    /**
     * Endpoint unificado para que qualquer usuário logado verifique a origem
     * e o status de seu acesso (seja pessoal ou via organização).
     * ESTE É O SUBSTITUTO DO ANTIGO 'getMySubscription'.
     */
    @GetMapping("/me/access-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccessStatusDTO> getMyAccessStatus(@AuthenticationPrincipal AuthUser currentUser) {
        AccessStatusDTO status = subscriptionService.getAccessStatusForUser(currentUser);
        return ResponseEntity.ok(status);
    }

    /**
     * Retorna os planos disponíveis para o usuário LOGADO, de acordo com seu contexto
     * (se ele tem ou não uma organização).
     */
    @GetMapping("/available-plans") // 2. URL simplificada
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PlanResponse>> getAvailablePlansForUser(@AuthenticationPrincipal AuthUser currentUser) {
        List<Plan> availablePlans;

        if (currentUser.getMemberships() != null && !currentUser.getMemberships().isEmpty()) {
            // Se for membro, mostra apenas planos ENTERPRISE
            availablePlans = planService.findAvailablePlansByType(PlanType.ENTERPRISE);
        } else {
            // Se for usuário individual, mostra apenas planos INDIVIDUAL
            availablePlans = planService.findAvailablePlansByType(PlanType.INDIVIDUAL);
        }

        // 3. Mapeamento consistente usando o metodo 'fromEntity'
        List<PlanResponse> response = availablePlans.stream()
                .map(PlanResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    // 4. MÉTODOS OBSOLETOS REMOVIDOS
    // - O endpoint GET /plans foi movido para o PublicCatalogController, que é seu lugar correto.
    // - O endpoint GET /me/subscription foi substituído por /me/access-status, que é mais completo.
    // - O endpoint GET /organizations/{orgId}/subscriptions foi movido para um futuro 'OrgAdminController',
    //   pois a lógica dele está mais ligada à gestão da organização do que ao contexto do usuário comum.
}