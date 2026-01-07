package com.example.prospera.subscription.controller;

import com.example.prospera.subscription.dto.PlanCreateRequest;
import com.example.prospera.subscription.dto.PlanResponse;
import com.example.prospera.subscription.dto.PlanUpdateRequest;
import com.example.prospera.subscription.entities.Plan;
import com.example.prospera.subscription.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controlador REST responsável pelo gerenciamento administrativo de planos.
 * <p>
 * Este controlador fornece endpoints protegidos que permitem administradores do sistema
 * realizarem operações de criação e atualização de planos de assinatura.
 * Todas as operações requerem autenticação com papel SYSTEM_ADMIN.
 * <p>
 * Base path: /admin/plans
 */
@RestController
@RequestMapping("/admin/plans")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class AdminPlanController {

    private final PlanService planService;

    /**
     * Este endpoint permite que administradores criem novos planos de assinatura
     * com as características especificadas no corpo da requisição.
     *
     * @param request DTO contendo as informações necessárias para criar um novo plano
     * @return ResponseEntity com status 201 (CREATED) e o plano criado no corpo da resposta
     */
    @PostMapping
    public ResponseEntity<PlanResponse> createPlan(@RequestBody @Valid PlanCreateRequest request) {
        // 1. O PlanService já usa o 'request.type()' para definir o tipo do plano.
        Plan newPlan = planService.createPlan(request);

        // 2. Mapeia a entidade completa para o DTO de resposta.
        PlanResponse response = PlanResponse.fromEntity(newPlan);

        // 3. Retorna 201 Created com o objeto recém-criado.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Este endpoint permite que administradores modifiquem as características
     * de um plano existente identificado pelo seu ID único.
     *
     * @param planId ID único do plano a ser atualizado
     * @param request DTO contendo as informações a serem atualizadas no plano
     * @return ResponseEntity com status 200 (OK) e o plano atualizado no corpo da resposta
     */
    @PutMapping("/{planId}")
    public ResponseEntity<PlanResponse> updatePlan(
            @PathVariable UUID planId, @RequestBody @Valid PlanUpdateRequest request) {
        Plan updatedPlan = planService.updatePlan(planId, request);

        // Mapeia a entidade atualizada para o DTO de resposta.
        PlanResponse response = PlanResponse.fromEntity(updatedPlan);

        return ResponseEntity.ok(response);
    }
}