package com.example.docgen.courses.api;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.courses.service.ContentAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/stream") // Mantemos /stream para não quebrar as imagens
@RequiredArgsConstructor
public class ContentStreamController {

    private final ContentAccessService contentAccessService;

    /**
     * Endpoint PÚBLICO para servir imagens de capa.
     * Não tem @PreAuthorize, então será liberado pela configuração de segurança.
     */
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        Resource file = contentAccessService.loadImageResource(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(file);
    }

    /**
     * Endpoint RESTRITO para servir o conteúdo de e-books.
     * Requer que o usuário esteja autenticado. A lógica de negócio
     * (matrícula/assinatura) é verificada dentro do serviço.
     */
    @GetMapping("/ebooks/{trainingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> serveEbook(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID trainingId) {

        Resource file = contentAccessService.loadEbookForUser(user, trainingId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(file);
    }
}