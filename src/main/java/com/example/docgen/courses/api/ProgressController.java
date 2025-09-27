package com.example.docgen.courses.api;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.courses.api.dto.EbookProgressDTO;
import com.example.docgen.courses.service.ProgressService;
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

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    // DTO para receber o progresso do Ebook
    public record EbookProgressRequest(int lastPageRead) {
    }

    @PutMapping("/ebooks/{trainingId}")
    public ResponseEntity<Void> updateEbookProgress(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID trainingId,
            @RequestBody EbookProgressRequest request) {
        progressService.updateEbookProgress(user.getId(), trainingId, request.lastPageRead());
        return ResponseEntity.ok().build();
    }

    /**
     * NOVO ENDPOINT:
     * Retorna o progresso do usuário logado em um e-book específico.
     */
    @GetMapping("/ebooks/{trainingId}")
    public ResponseEntity<EbookProgressDTO> getEbookProgress(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID trainingId) {

        EbookProgressDTO progress = progressService.getEbookProgress(user.getId(), trainingId);
        return ResponseEntity.ok(progress);
    }
}