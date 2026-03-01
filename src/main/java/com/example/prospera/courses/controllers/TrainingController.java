package com.example.prospera.courses.controllers;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.courses.dto.EnrollmentResponseDTO;
import com.example.prospera.courses.dto.ModuleDTO;
import com.example.prospera.courses.dto.RatingRequestDTO;
import com.example.prospera.courses.dto.TrainingCatalogItemDTO;
import com.example.prospera.courses.service.EnrollmentService;
import com.example.prospera.courses.service.ProgressService;
import com.example.prospera.courses.service.TrainingCatalogService;
import com.example.prospera.courses.service.TrainingRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST responsável por gerenciar operações relacionadas a treinamentos.
 * Fornece endpoints para visualização do catálogo, matrículas, progresso e
 * gerenciamento de treinamentos do usuário.
 */
@RestController
@RequestMapping("/trainings")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingCatalogService trainingCatalogService;
    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;
    private final TrainingRatingService ratingService;

    /**
     * Retorna o catálogo de treinamentos disponíveis para o usuário autenticado.
     *
     * @param user Usuário autenticado na requisição
     * @return ResponseEntity contendo lista de itens do catálogo de treinamentos
     */
    @GetMapping("/catalog")
    public ResponseEntity<List<TrainingCatalogItemDTO>> getMyCatalog(@AuthenticationPrincipal AuthUser user) {
        List<TrainingCatalogItemDTO> catalog = trainingCatalogService.getCatalogForUser(user);
        return ResponseEntity.ok(catalog);
    }

    /**
     * Realiza a matrícula do usuário autenticado em um treinamento específico.
     *
     * @param user       Usuário autenticado na requisição
     * @param trainingId ID do treinamento para matrícula
     * @return ResponseEntity contendo os dados da nova matrícula
     */
    @PostMapping("/{trainingId}/enroll")
    public ResponseEntity<EnrollmentResponseDTO> enrollInTraining(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID trainingId) {
        EnrollmentResponseDTO newEnrollmentDTO = enrollmentService.enrollUserInTraining(user, trainingId);
        return ResponseEntity.ok(newEnrollmentDTO);
    }

    /**
     * Marca uma lição específica como concluída para o usuário autenticado.
     *
     * @param user     Usuário autenticado na requisição
     * @param lessonId ID da lição a ser marcada como concluída
     * @return ResponseEntity sem conteúdo indicando sucesso da operação
     */
    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<Void> markLessonAsCompleted(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID lessonId) {
        progressService.markLessonAsCompleted(user.getId(), lessonId);
        return ResponseEntity.ok().build();
    }

    /**
     * Retorna todas as matrículas ativas do usuário autenticado.
     *
     * @param user Usuário autenticado na requisição
     * @return ResponseEntity contendo lista de matrículas do usuário
     */
    @GetMapping("/my-enrollments")
    public ResponseEntity<List<EnrollmentResponseDTO>> getMyEnrollments(@AuthenticationPrincipal AuthUser user) {
        List<EnrollmentResponseDTO> enrollments = enrollmentService.findEnrollmentsForUser(user);
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Retorna a estrutura de módulos e aulas de um treinamento para o aluno assistir.
     * Deve validar se o aluno possui uma matrícula ativa neste treinamento.
     *
     * @param user       Usuário autenticado
     * @param trainingId ID do treinamento
     * @return Lista de módulos com suas respectivas aulas
     */
    @GetMapping("/{trainingId}/modules")
    public ResponseEntity<List<ModuleDTO>> getTrainingModules(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID trainingId) {

        // 1. Verificar se o usuário tem matrícula ativa no trainingId.
        // 2. Buscar os módulos e aulas desse treinamento.
        // 3. Retornar os DTOs.
        List<ModuleDTO> modules = trainingCatalogService.findModulesForStudent(user, trainingId);

        return ResponseEntity.ok(modules);
    }

    @PostMapping("/{trainingId}/rate")
    public ResponseEntity<Void> rateTraining(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID trainingId,
            @RequestBody @Valid RatingRequestDTO dto) {

        ratingService.rateTraining(user, trainingId, dto);
        return ResponseEntity.ok().build();
    }
}