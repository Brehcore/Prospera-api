package com.example.prospera.subscription.controller;

import com.example.prospera.subscription.dto.PlanIdRequest;
import com.example.prospera.subscription.dto.SubscriptionCreateRequest;
import com.example.prospera.subscription.dto.SubscriptionResponse;
import com.example.prospera.subscription.entities.Subscription;
import com.example.prospera.subscription.enums.SubscriptionOrigin;
import com.example.prospera.subscription.enums.SubscriptionStatus;
import com.example.prospera.subscription.service.SubscriptionService;
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

/**
 * Controlador REST para gerenciamento administrativo de assinaturas no sistema.
 * Fornece endpoints para criar, cancelar e listar assinaturas tanto pessoais quanto empresariais.
 * <p>
 * Todas as operações requerem autenticação com papel SYSTEM_ADMIN.
 * <p>
 * Endpoints disponíveis:
 * - POST /admin/subscriptions/users: Cria assinatura pessoal
 * - POST /admin/subscriptions/organizations/{organizationId}: Cria assinatura empresarial
 * - POST /admin/subscriptions/{subscriptionId}/cancel: Cancela uma assinatura
 * - GET /admin/subscriptions: Lista todas as assinaturas com filtros opcionais
 *
 * @see com.example.prospera.subscription.service.SubscriptionService
 * @see com.example.prospera.subscription.entities.Subscription
 */
@RestController
@RequestMapping("/admin/subscriptions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AdminSubscriptionController {

    /**
     * Serviço responsável pela lógica de negócio relacionada às assinaturas.
     */
    private final SubscriptionService subscriptionService;

    /**
     * Cria uma nova assinatura pessoal para um usuário específico.
     * O backend encontra ou cria a "Conta Pessoal" do usuário e atrela a assinatura a ela.
     * Endpoint: POST /admin/subscriptions/users
     * Requer: Body com userId e planId
     * Retorna: SubscriptionResponse com os detalhes da nova assinatura criada
     * Status: 201 Created em caso de sucesso
     */
    @PostMapping("/users")
    public ResponseEntity<SubscriptionResponse> createPersonalSubscription(@RequestBody @Valid SubscriptionCreateRequest request) {
        Subscription newSubscription = subscriptionService.createPersonalSubscription(request.userId(), request.planId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToSubscriptionResponse(newSubscription));
    }

    /**
     * Cria uma nova assinatura Enterprise.
     * O backend encontra a "Conta Cliente" (Account) da organização especificada e atrela a assinatura a essa conta inteira.
     * Endpoint: POST /admin/subscriptions/organizations/{organizationId}
     * Requer: Path variable organizationId e body com planId
     * Retorna: SubscriptionResponse com os detalhes da nova assinatura empresarial
     * Status: 201 Created em caso de sucesso
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
     * Endpoint: POST /admin/subscriptions/{subscriptionId}/cancel
     * Requer: Path variable subscriptionId
     * Retorna: Sem conteúdo
     * Status: 204 No Content em caso de sucesso
     */
    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<Void> cancelSubscription(@PathVariable UUID subscriptionId) {
        subscriptionService.cancelSubscription(subscriptionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista todas as assinaturas do sistema.
     * Permite filtrar os resultados usando os parâmetros ?status= (ex: ACTIVE) e ?origin= (ex: MANUAL).
     * Endpoint: GET /admin/subscriptions
     * Parâmetros opcionais: 
     *   - status: Filtra por SubscriptionStatus (ACTIVE, CANCELED, etc)
     *   - origin: Filtra por SubscriptionOrigin (MANUAL, SYSTEM, etc)
     * Retorna: Lista de SubscriptionResponse com todas as assinaturas que atendem aos filtros
     * Status: 200 OK em caso de sucesso
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
     * Converte uma entidade Subscription para seu DTO de resposta (SubscriptionResponse).
     *
     * @param subscription A entidade Subscription a ser convertida
     * @return DTO SubscriptionResponse contendo os dados formatados da assinatura
     */
    private SubscriptionResponse mapToSubscriptionResponse(Subscription subscription) {
        // Inicializa as variáveis do "dono"
        UUID ownerAccountId = subscription.getAccount().getId();
        String ownerName;
        UUID ownerUserId = null;

        // Verifica se a conta da assinatura é uma conta pessoal
        if (subscription.getAccount().getPersonalUser() != null) {
            // Se sim, é uma assinatura INDIVIDUAL
            ownerName = subscription.getAccount().getPersonalUser().getEmail(); // Ou getFullName(), se preferir
            ownerUserId = subscription.getAccount().getPersonalUser().getId();
        } else {
            // Se não, é uma assinatura ENTERPRISE
            ownerName = subscription.getAccount().getName();
        }

        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getPlan().getId(),
                subscription.getPlan().getName(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getStatus(),
                subscription.getOrigin(),
                // Preenche os novos campos do "dono"
                ownerAccountId,
                ownerName,
                ownerUserId
        );
    }
}