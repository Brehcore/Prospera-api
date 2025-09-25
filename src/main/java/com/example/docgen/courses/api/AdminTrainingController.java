package com.example.docgen.courses.api;

import com.example.docgen.courses.api.dto.LessonCreateRequest;
import com.example.docgen.courses.api.dto.LessonDTO;
import com.example.docgen.courses.api.dto.ModuleCreateRequest;
import com.example.docgen.courses.api.dto.ModuleDTO;
import com.example.docgen.courses.api.dto.TrainingCreateRequest;
import com.example.docgen.courses.api.dto.TrainingDTO;
import com.example.docgen.courses.api.dto.TrainingDetailDTO;
import com.example.docgen.courses.api.dto.TrainingSectorAssignmentRequest;
import com.example.docgen.courses.api.dto.TrainingSummaryDTO;
import com.example.docgen.courses.api.dto.TrainingUpdateRequest;
import com.example.docgen.courses.domain.enums.PublicationStatus;
import com.example.docgen.courses.domain.enums.TrainingEntityType;
import com.example.docgen.courses.service.AdminTrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/trainings")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class AdminTrainingController {

    // A injeção de dependência acontece aqui
    private final AdminTrainingService adminTrainingService;

    @PostMapping
    public ResponseEntity<TrainingDTO> createTraining(@RequestBody @Valid TrainingCreateRequest dto) {
        TrainingDTO newTraining = adminTrainingService.createTraining(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(newTraining.id()).toUri();
        return ResponseEntity.created(location).body(newTraining);
    }

    @PostMapping("/{trainingId}/publish")
    public ResponseEntity<Void> publishTraining(@PathVariable UUID trainingId) {
        adminTrainingService.changeTrainingStatus(trainingId, PublicationStatus.PUBLISHED);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{trainingId}/sectors")
    public ResponseEntity<Void> assignTrainingToSector(
            @PathVariable UUID trainingId,
            @RequestBody @Valid TrainingSectorAssignmentRequest dto) {
        adminTrainingService.assignTrainingToSector(trainingId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/courses/{trainingId}/modules")
    public ResponseEntity<ModuleDTO> addModuleToRecordedCourse(
            @PathVariable UUID trainingId,
            @RequestBody @Valid ModuleCreateRequest dto) {
        ModuleDTO newModule = adminTrainingService.addModuleToTraining(trainingId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newModule);
    }

    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<LessonDTO> addLessonToModule(
            @PathVariable UUID moduleId,
            @RequestBody @Valid LessonCreateRequest dto) {
        LessonDTO newLesson = adminTrainingService.addLessonToModule(moduleId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newLesson);
    }

    @PostMapping("/ebooks/{trainingId}/upload")
    public ResponseEntity<Void> uploadEbookFile(
            @PathVariable UUID trainingId,
            @RequestParam("file") MultipartFile file) {
        adminTrainingService.setEbookFile(trainingId, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<TrainingSummaryDTO>> getAllTrainings(
            @RequestParam(required = false) TrainingEntityType type) {

        List<TrainingSummaryDTO> trainings = adminTrainingService.findAll(type);
        return ResponseEntity.ok(trainings);
    }

    /**
     * Busca um treinamento específico pelo ID, retornando todos os seus detalhes.
     */
    @GetMapping("/{trainingId}")
// Altere o tipo de retorno aqui também
    public ResponseEntity<TrainingDetailDTO> getTrainingDetails(@PathVariable UUID trainingId) {
        // A chamada ao serviço agora retorna um TrainingDetailDTO
        TrainingDetailDTO training = adminTrainingService.getTrainingById(trainingId);
        return ResponseEntity.ok(training);
    }

    /**
     * Atualiza os dados de um treinamento existente.
     */
    @PutMapping("/{trainingId}")
    public ResponseEntity<TrainingDTO> updateTraining(
            @PathVariable UUID trainingId,
            @RequestBody @Valid TrainingUpdateRequest dto) {

        TrainingDTO updatedTraining = adminTrainingService.updateTraining(trainingId, dto);
        return ResponseEntity.ok(updatedTraining);
    }

    /**
     * Exclui um treinamento do sistema.
     */
    @DeleteMapping("/{trainingId}")
    public ResponseEntity<Void> deleteTraining(@PathVariable UUID trainingId) {
        adminTrainingService.deleteTraining(trainingId);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }

    @PostMapping("/{trainingId}/cover-image")
    public ResponseEntity<Void> uploadCoverImage(
            @PathVariable UUID trainingId,
            @RequestParam("file") MultipartFile file) {

        adminTrainingService.setCoverImage(trainingId, file);
        return ResponseEntity.ok().build();
    }
}