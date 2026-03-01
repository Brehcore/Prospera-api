package com.example.prospera.courses.controllers;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.courses.domain.Lesson;
import com.example.prospera.courses.dto.LessonDTO;
import com.example.prospera.courses.service.LessonService;
import com.example.prospera.courses.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controlador REST responsável por gerenciar operações relacionadas às lições do curso.
 * Requer autenticação para acessar todos os endpoints.
 */
@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class LessonController {


    private final LessonService lessonService;
    private final ProgressService progressService;

    /**
     * Retorna a próxima lição na sequência do curso.
     *
     * @param lessonId ID da lição atual
     * @return ResponseEntity contendo a próxima lição se existir, ou status 204 (No Content) caso contrário
     */
    @GetMapping("/{lessonId}/next")
    public ResponseEntity<?> getNextLesson(@PathVariable UUID lessonId) {
        return lessonService.findNextLesson(lessonId)
                // Reutilizando seu metodo estático fromEntity para mapear
                .map(lesson -> ResponseEntity.ok(LessonDTO.fromEntity(lesson)))
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Retorna a lição anterior na sequência do curso.
     *
     * @param lessonId ID da lição atual
     * @return ResponseEntity contendo a lição anterior se existir, ou status 204 (No Content) caso contrário
     */
    @GetMapping("/{lessonId}/previous")
    public ResponseEntity<?> getPreviousLesson(@PathVariable UUID lessonId) {
        return lessonService.findPreviousLesson(lessonId)
                .map(lesson -> ResponseEntity.ok(LessonDTO.fromEntity(lesson)))
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Endpoint principal para o Player de Vídeo.
     * Retorna o JSON com título, descrição e a URL do vídeo.
     */
    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonDTO> getLesson(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID lessonId) {

        Lesson lesson = lessonService.findLessonForUser(lessonId, user);

        // Verifica se já foi concluída para preencher o DTO
        boolean isCompleted = lessonService.isLessonCompleted(lessonId, user.getId());

        return ResponseEntity.ok(LessonDTO.fromEntity(lesson, isCompleted));
    }

    /**
     * Endpoint para marcar a aula como concluída (100% assistida).
     * Deve ser chamado pelo frontend quando o vídeo terminar ou passar de X%.
     */
    @PostMapping("/{lessonId}/complete")
    public ResponseEntity<Void> completeLesson(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID lessonId) {

        progressService.markLessonAsCompleted(user.getId(), lessonId);
        return ResponseEntity.ok().build();
    }


}