package com.example.docgen.courses.api;

import com.example.docgen.courses.api.dto.PublicTrainingDTO;
import com.example.docgen.courses.service.TrainingCatalogService;
import com.example.docgen.enterprise.api.dto.SectorDTO;
import com.example.docgen.enterprise.service.SectorService;
import com.example.docgen.subscription.dto.PlanResponse;
import com.example.docgen.subscription.entities.Plan;
import com.example.docgen.subscription.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/public/catalog") // Um prefixo /public deixa claro que é uma rota aberta
@RequiredArgsConstructor
public class PublicCatalogController {

    private final TrainingCatalogService trainingCatalogService;
    private final SectorService sectorService;
    private final PlanService planService;

    /**
     * Lista todos os treinamentos publicados para a vitrine pública.
     */
    @GetMapping
    public ResponseEntity<List<PublicTrainingDTO>> listPublicTrainings() {
        List<PublicTrainingDTO> catalog = trainingCatalogService.findAllPublishedForPublic();
        return ResponseEntity.ok(catalog);
    }

    /**
     * Mostra os detalhes públicos de um treinamento específico.
     */
    @GetMapping("/{trainingId}")
    public ResponseEntity<PublicTrainingDTO> getPublicTrainingDetails(@PathVariable UUID trainingId) {
        PublicTrainingDTO training = trainingCatalogService.findPublishedByIdForPublic(trainingId);
        return ResponseEntity.ok(training);
    }

    /**
     * NOVO ENDPOINT:
     * Lista todos os setores globais disponíveis para serem usados como filtro.
     */
    @GetMapping("/sectors")
    public ResponseEntity<List<SectorDTO>> listPublicSectors() {
        List<SectorDTO> sectors = sectorService.findAllPublicSectors();
        return ResponseEntity.ok(sectors);
    }

    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>> listPublicPlans() {
        // Chama o metodo que já existe no PlanService
        List<Plan> activePlans = planService.getActivePlans();

        // Mapeia a lista de entidades para a lista de DTOs usando o método estático
        List<PlanResponse> response = activePlans.stream()
                .map(PlanResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }
}