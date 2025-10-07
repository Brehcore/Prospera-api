package com.example.docgen.subscription.controller;

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

    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(@RequestBody @Valid SubscriptionCreateRequest request) {
        Subscription newSubscription = subscriptionService.createSubscriptionForUser(request.userId(), request.planId());

        SubscriptionResponse response = new SubscriptionResponse(
                newSubscription.getId(),
                newSubscription.getUser().getId(),
                newSubscription.getPlan().getId(),
                newSubscription.getPlan().getName(),
                newSubscription.getStartDate(),
                newSubscription.getEndDate(),
                newSubscription.getStatus(),
                newSubscription.getOrigin()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<Void> cancelSubscription(@PathVariable UUID subscriptionId) {
        subscriptionService.cancelSubscription(subscriptionId);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }

    /**
     * Endpoint para listar todas as assinaturas do sistema.
     * Permite filtrar por status e origem.
     */
    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> listAllSubscriptions(
            @RequestParam(required = false) SubscriptionStatus status,
            @RequestParam(required = false) SubscriptionOrigin origin
    ) {
        List<Subscription> subscriptions = subscriptionService.findAllSubscriptions(status, origin);

        // Mapeia a lista de entidades para uma lista de DTOs de resposta
        List<SubscriptionResponse> response = subscriptions.stream()
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
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
