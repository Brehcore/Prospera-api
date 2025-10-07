package com.example.docgen.subscription.service;

import com.example.docgen.auth.exceptions.BusinessRuleException;
import com.example.docgen.auth.exceptions.ResourceNotFoundException;
import com.example.docgen.subscription.dto.PlanCreateRequest;
import com.example.docgen.subscription.dto.PlanUpdateRequest;
import com.example.docgen.subscription.entities.Plan;
import com.example.docgen.subscription.repositories.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional
    public Plan createPlan(PlanCreateRequest dto) {
        // Verificação 1: Nome duplicado
        if (planRepository.existsByName(dto.name())) {
            throw new BusinessRuleException("Já existe um plano com o nome: " + dto.name());
        }

        // Verificação 2: Lógica de preço
        if (dto.currentPrice().compareTo(dto.originalPrice()) > 0) {
            throw new BusinessRuleException("O preço atual (com desconto) não pode ser maior que o preço original.");
        }

        Plan newPlan = new Plan();
        newPlan.setName(dto.name());
        newPlan.setDescription(dto.description());
        newPlan.setOriginalPrice(dto.originalPrice());
        newPlan.setCurrentPrice(dto.currentPrice());
        newPlan.setDurationInDays(dto.durationInDays());
        newPlan.setActive(true);

        return planRepository.save(newPlan);
    }


    public List<Plan> getActivePlans() {
        return planRepository.findAllByIsActive(true);
    }

    @Transactional
    public Plan updatePlan(UUID planId, PlanUpdateRequest requestDto) {
        // 1. Busca o plano existente no banco de dados.
        Plan existingPlan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano com ID " + planId + " não encontrado."));

        // 2. Valida a lógica de preço (igual à da criação)
        if (requestDto.currentPrice().compareTo(requestDto.originalPrice()) > 0) {
            throw new BusinessRuleException("O preço atual (com desconto) não pode ser maior que o preço original.");
        }

        // 3. Atualiza os campos do objeto com os novos dados do DTO.
        existingPlan.setName(requestDto.name());
        existingPlan.setDescription(requestDto.description());
        existingPlan.setOriginalPrice(requestDto.originalPrice());
        existingPlan.setCurrentPrice(requestDto.currentPrice());
        existingPlan.setDurationInDays(requestDto.durationInDays());
        existingPlan.setActive(requestDto.isActive());

        // 4. Salva a entidade atualizada. O JPA entende que é um UPDATE, pois a entidade já tem um ID.
        return planRepository.save(existingPlan);
    }
}
