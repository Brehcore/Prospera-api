package com.example.docgen.courses.api;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.courses.api.dto.EnrollmentResponseDTO;
import com.example.docgen.courses.api.dto.TrainingCatalogItemDTO;
import com.example.docgen.courses.service.AdminTrainingService;
import com.example.docgen.courses.service.EnrollmentService;
import com.example.docgen.courses.service.ProgressService;
import com.example.docgen.courses.service.TrainingCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trainings")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingCatalogService trainingCatalogService;
    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;
    private final AdminTrainingService adminTrainingService;

    @GetMapping("/catalog")
    public ResponseEntity<List<TrainingCatalogItemDTO>> getMyCatalog(@AuthenticationPrincipal AuthUser user) {
        List<TrainingCatalogItemDTO> catalog = trainingCatalogService.getCatalogForUser(user);
        return ResponseEntity.ok(catalog);
    }

    // VERIFICAR ENDPOINT PARA CORRIGIR
    @PostMapping("/{trainingId}/enroll")
    public ResponseEntity<EnrollmentResponseDTO> enrollInTraining(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID trainingId) {
        var newEnrollment = enrollmentService.enrollUserInTraining(user, trainingId);
        return ResponseEntity.ok(EnrollmentResponseDTO.fromEntity(newEnrollment));
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<Void> markLessonAsCompleted(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID lessonId) {
        progressService.markLessonAsCompleted(user.getId(), lessonId);
        return ResponseEntity.ok().build();
    }
}