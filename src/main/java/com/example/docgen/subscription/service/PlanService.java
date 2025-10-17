package com.example.docgen.subscription.service;

import com.example.docgen.auth.exceptions.BusinessRuleException;
import com.example.docgen.auth.exceptions.ResourceNotFoundException;
import com.example.docgen.subscription.dto.PlanCreateRequest;
import com.example.docgen.subscription.dto.PlanUpdateRequest;
import com.example.docgen.subscription.entities.Plan;
import com.example.docgen.subscription.enums.PlanType;
import com.example.docgen.subscription.repositories.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional
    public Plan createPlan(PlanCreateRequest dto) {
        // Validações centralizadas
        validatePlanNameIsUnique(dto.name(), null);
        validatePriceLogic(dto.currentPrice(), dto.originalPrice());

        // Mapeamento centralizado
        Plan newPlan = new Plan();
        mapDtoToEntity(newPlan, dto.name(), dto.description(), dto.originalPrice(), dto.currentPrice(), dto.durationInDays(), true, dto.type());

        return planRepository.save(newPlan);
    }

    @Transactional
    public Plan updatePlan(UUID planId, PlanUpdateRequest dto) {
        Plan existingPlan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano com ID " + planId + " não encontrado."));

        // Validações centralizadas, agora incluindo a verificação de nome duplicado
        validatePlanNameIsUnique(dto.name(), planId);
        validatePriceLogic(dto.currentPrice(), dto.originalPrice());

        // Mapeamento centralizado
        mapDtoToEntity(existingPlan, dto.name(), dto.description(), dto.originalPrice(), dto.currentPrice(), dto.durationInDays(), dto.isActive(), null); // Type não é atualizável

        return planRepository.save(existingPlan);
    }

    @Transactional(readOnly = true)
    public List<Plan> getActivePlans() {
        return planRepository.findAllByIsActive(true);
    }

    @Transactional(readOnly = true)
    public List<Plan> findAvailablePlansByType(PlanType type) {
        return planRepository.findByTypeAndIsActive(type, true);
    }

    // =============================================================
    // MÉTODOS PRIVADOS AUXILIARES PARA REUTILIZAÇÃO DE CÓDIGO
    // =============================================================

    /**
     * Centraliza a lógica de validação de nome único.
     *
     * @param name          Nome a ser verificado.
     * @param currentPlanId O ID do plano atual (nulo se for uma criação).
     */
    private void validatePlanNameIsUnique(String name, UUID currentPlanId) {
        Optional<Plan> planByName = planRepository.findByName(name);
        if (planByName.isPresent() && !planByName.get().getId().equals(currentPlanId)) {
            throw new BusinessRuleException("Já existe um plano com o nome: " + name);
        }
    }

    /**
     * Centraliza a lógica de validação de preços.
     */
    private void validatePriceLogic(BigDecimal currentPrice, BigDecimal originalPrice) {
        if (currentPrice.compareTo(originalPrice) > 0) {
            throw new BusinessRuleException("O preço atual (com desconto) não pode ser maior que o preço original.");
        }
    }

    /**
     * Centraliza o mapeamento de dados de um DTO para a entidade Plan.
     */
    private void mapDtoToEntity(Plan plan, String name, String description, BigDecimal originalPrice,
                                BigDecimal currentPrice, int duration, boolean isActive, PlanType type) {
        plan.setName(name);
        plan.setDescription(description);
        plan.setOriginalPrice(originalPrice);
        plan.setCurrentPrice(currentPrice);
        plan.setDurationInDays(duration);
        plan.setActive(isActive);

        // O tipo só é definido na criação, nunca na atualização
        if (type != null) {
            plan.setType(type);
        }
    }
}