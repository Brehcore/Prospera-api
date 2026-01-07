package com.example.prospera.subscription.service;

import com.example.prospera.auth.exceptions.BusinessRuleException;
import com.example.prospera.auth.exceptions.ResourceNotFoundException;
import com.example.prospera.subscription.dto.PlanCreateRequest;
import com.example.prospera.subscription.dto.PlanUpdateRequest;
import com.example.prospera.subscription.entities.Plan;
import com.example.prospera.subscription.enums.PlanType;
import com.example.prospera.subscription.repositories.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço responsável pelo gerenciamento de planos de assinatura.
 * <p>
 * Este serviço fornece operações para:
 * - Criar novos planos
 * - Atualizar planos existentes
 * - Consultar planos ativos
 * - Buscar planos por tipo
 * <p>
 * Realiza validações de negócio como:
 * - Unicidade do nome do plano
 * - Lógica de preços (atual vs original)
 *
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    /**
     * Cria um novo plano de assinatura.
     *
     * @param dto Dados do plano a ser criado
     * @return Plano criado e persistido
     * @throws BusinessRuleException se o nome já existir ou se a lógica de preços for inválida
     */
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

    /**
     * Atualiza um plano existente.
     *
     * @param planId ID do plano a ser atualizado
     * @param dto    Dados atualizados do plano
     * @return Plano atualizado
     * @throws ResourceNotFoundException se o plano não for encontrado
     * @throws BusinessRuleException     se o nome já existir ou se a lógica de preços for inválida
     */
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

    /**
     * Retorna todos os planos ativos.
     *
     * @return Lista de planos ativos
     */
    @Transactional(readOnly = true)
    public List<Plan> getActivePlans() {
        return planRepository.findAllByIsActive(true);
    }

    /**
     * Busca planos ativos por tipo.
     *
     * @param type Tipo do plano
     * @return Lista de planos ativos do tipo especificado
     */
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