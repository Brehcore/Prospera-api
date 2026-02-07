package com.example.prospera.certificate.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

// DELETAR DEPOIS DE CONCLUIR OS TESTES

@RestController
@RequestMapping("/public/preview")
@RequiredArgsConstructor
public class CertificatePreviewController {

    private final TemplateEngine templateEngine;

    @GetMapping(value = "/certificate", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> previewCertificateHtml() {
        Context context = new Context();

        // 1. Dados Fictícios para teste visual
        context.setVariable("studentName", "João da Silva Sauro");
        context.setVariable("courseName", "Curso Avançado de Java Spring Boot");
        context.setVariable("workload", "40 horas");
        context.setVariable("validationCode", "ABCD-1234");
        context.setVariable("completionDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // 2. Carrega a imagem e converte para Base64 (Igual ao Service)
        try {
            // ATENÇÃO: O nome aqui deve ser EXATAMENTE igual ao arquivo na pasta resources
            ClassPathResource imageFile = new ClassPathResource("CERTIFICADO.png");
            byte[] imageBytes = StreamUtils.copyToByteArray(imageFile.getInputStream());
            String bgImageBase64 = Base64.getEncoder().encodeToString(imageBytes);

            context.setVariable("bgImageBase64", bgImageBase64);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Erro ao carregar imagem: " + e.getMessage());
        }

        // 3. Processa o HTML e retorna como String
        String htmlContent = templateEngine.process("certificate", context);

        return ResponseEntity.ok(htmlContent);
    }
}