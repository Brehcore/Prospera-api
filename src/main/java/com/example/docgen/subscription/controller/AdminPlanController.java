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

    @PostMapping
    public ResponseEntity<Void> createPlan(
            @RequestBody @Valid PlanCreateRequest request) {
        planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{planId}")
    public ResponseEntity<PlanResponse> updatePlan(
            @PathVariable UUID planId, @RequestBody @Valid PlanUpdateRequest request) {
        Plan updatePlan = planService.updatePlan(planId, request);

        // Converte a entidade Plan para um DTO e retorna o DTO como resposta.
        PlanResponse response = new PlanResponse(
                updatePlan.getId(),
                updatePlan.getName(),
                updatePlan.getDescription(),
                updatePlan.getOriginalPrice(),
                updatePlan.getCurrentPrice(),
                updatePlan.getDurationInDays(),
                updatePlan.isActive()
        );
        // Retorna 200 OK com o DTO como corpo da resposta.
        return ResponseEntity.ok(response);
    }
}
