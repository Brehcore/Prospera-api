package com.example.docgen.subscription.controller;

import com.example.docgen.subscription.dto.PlanCreateRequest;
import com.example.docgen.subscription.dto.PlanResponse;
import com.example.docgen.subscription.dto.PlanUpdateRequest;
import com.example.docgen.subscription.entities.Plan;
import com.example.docgen.subscription.service.PlanService;
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

@RestController
@RequestMapping("/admin/plans")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class AdminPlanController {

    private final PlanService planService;

    /**
     * Cria um novo plano e o retorna no corpo da resposta.
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
     * Atualiza um plano existente e o retorna no corpo da resposta.
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