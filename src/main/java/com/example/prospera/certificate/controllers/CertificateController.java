package com.example.prospera.certificate.controllers;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.certificate.domain.Certificate;
import com.example.prospera.certificate.dto.CertificateListItemDTO;
import com.example.prospera.certificate.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    /**
     * Gera (se não existir) e retorna os dados do certificado.
     */
    @PostMapping("/issue/{enrollmentId}")
    public ResponseEntity<String> issueCertificate(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID enrollmentId) {

        // Adicione validação aqui para garantir que a matrícula pertence ao usuário logado!

        Certificate cert = certificateService.issueCertificate(enrollmentId);
        return ResponseEntity.ok(cert.getValidationCode());
    }

    /**
     * Faz o download do PDF.
     */
    @GetMapping("/download/{certificateId}")
    public ResponseEntity<Resource> downloadCertificate(@PathVariable UUID certificateId) {
        Resource file = certificateService.getCertificateFile(certificateId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificado.pdf\"")
                .body(file);
    }

    /**
     * Lista todos os certificados do usuário logado com filtro opcional por nome do curso.
     */
    @GetMapping("/my-certificates")
    public ResponseEntity<List<CertificateListItemDTO>> getMyCertificates(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(required = false) String search // Opcional: ?search=Java
    ) {
        var certificates = certificateService.getMyCertificates(user.getId(), search);
        return ResponseEntity.ok(certificates);
    }

    /**
     * Retorna uma miniatura (imagem JPG) do certificado com os dados impressos.
     */
    @GetMapping(value = "/{certificateId}/thumbnail", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getCertificateThumbnail(@PathVariable UUID certificateId) {
        byte[] imageBytes = certificateService.generateCertificateThumbnail(certificateId);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageBytes);
    }
}