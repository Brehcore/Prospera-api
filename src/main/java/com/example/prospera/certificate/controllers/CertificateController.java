package com.example.prospera.certificate.controllers;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.certificate.domain.Certificate;
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
import org.springframework.web.bind.annotation.RestController;

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
}