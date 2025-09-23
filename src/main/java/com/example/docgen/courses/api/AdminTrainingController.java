package com.example.docgen.courses.api;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.courses.api.dto.*;
import com.example.docgen.courses.domain.enums.PublicationStatus;
import com.example.docgen.courses.domain.enums.TrainingEntityType;
import com.example.docgen.courses.service.AdminTrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
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
}