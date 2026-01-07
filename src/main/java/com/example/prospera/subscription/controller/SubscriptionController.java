package com.example.prospera.subscription.controller;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.subscription.dto.AccessStatusDTO;
import com.example.prospera.subscription.dto.PlanResponse;
import com.example.prospera.subscription.entities.Plan;
import com.example.prospera.subscription.enums.PlanType;
import com.example.prospera.subscription.service.PlanService;
import com.example.prospera.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador responsável por gerenciar operações relacionadas a assinaturas e planos.
 * Fornece endpoints para verificação de status de acesso e consulta de planos disponíveis
 * para usuários autenticados.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubscriptionController {

    private final PlanService planService;
    private final SubscriptionService subscriptionService;

    /**
     * Retorna o status de acesso do usuário atual.
     * Este endpoint verifica tanto assinaturas pessoais quanto acesso via organização.
     *
     * @param currentUser O usuário autenticado atual
     * @return ResponseEntity contendo o status de acesso do usuário
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
}