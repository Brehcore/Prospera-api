package com.example.docgen.courses.api;

import com.example.docgen.courses.api.dto.PublicTrainingDTO;
import com.example.docgen.courses.service.TrainingCatalogService;
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
}