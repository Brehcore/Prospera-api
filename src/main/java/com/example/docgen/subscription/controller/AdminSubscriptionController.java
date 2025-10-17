package com.example.docgen.subscription.controller;

import com.example.docgen.subscription.dto.PlanIdRequest;
import com.example.docgen.subscription.dto.SubscriptionCreateRequest;
import com.example.docgen.subscription.dto.SubscriptionResponse;
import com.example.docgen.subscription.entities.Subscription;
import com.example.docgen.subscription.enums.SubscriptionOrigin;
import com.example.docgen.subscription.enums.SubscriptionStatus;
import com.example.docgen.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/subscriptions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AdminSubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Cria uma nova assinatura pessoal para um usuário específico.
     * O backend encontra ou cria a "Conta Pessoal" do usuário e atrela a assinatura a ela.
     */
    @PostMapping("/users")
    public ResponseEntity<SubscriptionResponse> createPersonalSubscription(@RequestBody @Valid SubscriptionCreateRequest request) {
        Subscription newSubscription = subscriptionService.createPersonalSubscription(request.userId(), request.planId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToSubscriptionResponse(newSubscription));
    }

    /**
     * Cria uma nova assinatura Enterprise.
     * O backend encontra a "Conta Cliente" (Account) da organização especificada e atrela a assinatura a essa conta inteira.
     */
    @PostMapping("/organizations/{organizationId}")
    public ResponseEntity<SubscriptionResponse> createOrganizationSubscription(
            @PathVariable UUID organizationId,
            @RequestBody @Valid PlanIdRequest request) {
        Subscription newSubscription = subscriptionService.createSubscriptionForOrganization(organizationId, request.planId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToSubscriptionResponse(newSubscription));
    }

    /**
     * Cancela uma assinatura ativa, mudando seu status para CANCELED e a data de término para o momento atual.
     */
    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<Void> cancelSubscription(@PathVariable UUID subscriptionId) {
        subscriptionService.cancelSubscription(subscriptionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista todas as assinaturas do sistema.
     * Permite filtrar os resultados usando os parâmetros ?status= (ex: ACTIVE) e ?origin= (ex: MANUAL).
     */
    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> listAllSubscriptions(
            @RequestParam(required = false) SubscriptionStatus status,
            @RequestParam(required = false) SubscriptionOrigin origin) {
        List<Subscription> subscriptions = subscriptionService.findAllSubscriptions(status, origin);
        List<SubscriptionResponse> response = subscriptions.stream()
                .map(this::mapToSubscriptionResponse) // Reutiliza o metodo de mapeamento
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Metodo privado auxiliar para mapear a entidade Subscription para o DTO de resposta.
     * Centraliza a lógica e evita NullPointerExceptions.
     */
    private SubscriptionResponse mapToSubscriptionResponse(Subscription subscription) {
        UUID userId = null;
        if (subscription.getAccount() != null && subscription.getAccount().getPersonalUser() != null) {
            userId = subscription.getAccount().getPersonalUser().getId();
        }

        return new SubscriptionResponse(
                subscription.getId(),
                userId,
                subscription.getPlan().getId(),
                subscription.getPlan().getName(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getStatus(),
                subscription.getOrigin()
        );
    }
}