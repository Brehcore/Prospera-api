package com.example.docgen.enterprise.api.dto;

import com.example.docgen.courses.api.dto.TrainingSummaryDTO;
import com.example.docgen.courses.service.TrainingCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sectors")
@RequiredArgsConstructor
public class SectorController {

    private final TrainingCatalogService trainingCatalogService;

    /**
     * Endpoint para ver os treinamentos de um setor específico.
     * Aberto para qualquer usuário autenticado.
     */
    @GetMapping("/{sectorId}/trainings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TrainingSummaryDTO>> getTrainingsBySector(@PathVariable UUID sectorId) {

        List<TrainingSummaryDTO> trainings = trainingCatalogService.findTrainingsBySector(sectorId);

        return ResponseEntity.ok(trainings);
    }
}
