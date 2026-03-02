package com.example.prospera.courses.controllers;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.courses.dto.EnrollmentResponseDTO;
import com.example.prospera.courses.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controlador responsável por gerenciar as matrículas (self-service) dos usuários.
 */
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Endpoint para o próprio usuário se matricular em um treinamento.
     */
    @PostMapping("/{trainingId}")
    public ResponseEntity<EnrollmentResponseDTO> selfEnroll(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID trainingId) {

        EnrollmentResponseDTO response = enrollmentService.enrollUserInTraining(user, trainingId);
        return ResponseEntity.ok(response);
    }
}