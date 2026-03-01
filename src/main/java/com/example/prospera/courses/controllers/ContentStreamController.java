package com.example.prospera.courses.controllers;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.courses.service.ContentAccessService;
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

/**
 * Controlador REST responsável pelo streaming de conteúdo estático como imagens e e-books.
 * Fornece endpoints para servir imagens de capa publicamente e e-books de forma restrita.
 * A rota base '/stream' é mantida para compatibilidade com links de imagens existentes.
 */
@RestController
@RequestMapping("/stream") // Mantemos /stream para não quebrar as imagens
@RequiredArgsConstructor
public class ContentStreamController {

    private final ContentAccessService contentAccessService;

    /**
     * Endpoint público para servir imagens de capa dos cursos.
     * Este endpoint não requer autenticação e está disponível para acesso público.
     *
     * @param filename Nome do arquivo de imagem a ser carregado
     * @return ResponseEntity contendo o recurso da imagem com content-type apropriado
     */
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        Resource file = contentAccessService.loadImageResource(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(file);
    }

    /**
     * Endpoint restrito para servir conteúdo de e-books.
     * Requer autenticação do usuário e verifica permissões de acesso através do ContentAccessService.
     *
     * @param user       Usuário autenticado atual obtido do contexto de segurança
     * @param trainingId ID do treinamento ao qual o e-book pertence
     * @return ResponseEntity contendo o recurso do e-book com headers apropriados para visualização
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