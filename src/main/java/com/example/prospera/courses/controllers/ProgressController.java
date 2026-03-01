package com.example.prospera.courses.controllers;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.courses.dto.EbookProgressDTO;
import com.example.prospera.courses.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controlador REST responsável por gerenciar o progresso dos usuários em e-books.
 * Fornece endpoints para atualizar e consultar o progresso da leitura.
 */
@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    /**
     * @param lastPageRead Número da última página lida pelo usuário
     */
    public record EbookProgressRequest(int lastPageRead) {
    }

    /**
     * @param user       Usuário autenticado na requisição
     * @param trainingId ID do treinamento/e-book
     * @param request    Objeto contendo a última página lida
     * @return ResponseEntity sem conteúdo indicando sucesso da operação
     */
    @PutMapping("/ebooks/{trainingId}")
    public ResponseEntity<Void> updateEbookProgress(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID trainingId,
            @RequestBody EbookProgressRequest request) {
        progressService.updateEbookProgress(user.getId(), trainingId, request.lastPageRead());
        return ResponseEntity.ok().build();
    }

    /**
     * Retorna o progresso atual do usuário em um e-book específico.
     *
     * @param user       Usuário autenticado na requisição
     * @param trainingId ID do treinamento/e-book
     * @return ResponseEntity contendo o DTO com informações do progresso
     */
    @GetMapping("/ebooks/{trainingId}")
    public ResponseEntity<EbookProgressDTO> getEbookProgress(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID trainingId) {

        EbookProgressDTO progress = progressService.getEbookProgress(user.getId(), trainingId);
        return ResponseEntity.ok(progress);
    }
}