package com.example.prospera.certificate.service;

import com.example.prospera.auth.exceptions.BusinessRuleException;
import com.example.prospera.certificate.domain.Certificate;
import com.example.prospera.certificate.repositories.CertificateRepository;
import com.example.prospera.courses.domain.Enrollment;
import com.example.prospera.courses.domain.enums.EnrollmentStatus;
import com.example.prospera.courses.repositories.EnrollmentRepository;
import com.example.prospera.courses.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FileStorageService fileStorageService;
    private final TemplateEngine templateEngine;

    @Transactional
    public Certificate issueCertificate(UUID enrollmentId) {
        // 1. Busca Matrícula
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessRuleException("Matrícula não encontrada."));

        // 2. Valida se o curso está concluído
        if (enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
            throw new BusinessRuleException("O certificado só pode ser emitido após a conclusão do curso.");
        }

        // 3. Valida se já existe (Retorna o existente para evitar duplicidade)
        if (certificateRepository.existsByEnrollmentId(enrollmentId)) {
            return certificateRepository.findByEnrollmentId(enrollmentId).orElseThrow();
        }

        // 4. Gera dados do Certificado
        String validationCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 5. Gera o PDF a partir do HTML (Thymeleaf + Flying Saucer)
        byte[] pdfBytes = generatePdfFromTemplate(enrollment, validationCode);

        // 6. Converte os bytes para MultipartFile para usar seu serviço de storage existente
        String fileName = "certificate_" + enrollmentId + ".pdf";
        MultipartFile multipartFile = new ByteArrayMultipartFile(pdfBytes, fileName, "application/pdf");

        // Salva o arquivo físico
        String storedPath = fileStorageService.save(multipartFile);

        // 7. Salva no Banco
        Certificate cert = Certificate.builder()
                .enrollment(enrollment)
                .validationCode(validationCode)
                .filePath(storedPath)
                .build();

        return certificateRepository.save(cert);
    }

    /**
     * Gera o PDF processando o template HTML com o Thymeleaf e convertendo com Flying Saucer.
     */
    private byte[] generatePdfFromTemplate(Enrollment enrollment, String validationCode) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 1. Contexto do Thymeleaf (Variáveis)
            Context context = new Context();

            // Tenta pegar o nome completo do perfil, senão usa o email
            String studentName = (enrollment.getUser().getPersonalProfile() != null)
                    ? enrollment.getUser().getPersonalProfile().getFullName()
                    : enrollment.getUser().getEmail();

            context.setVariable("studentName", studentName);
            context.setVariable("courseTitle", enrollment.getTraining().getTitle());
            context.setVariable("validationCode", validationCode);
            context.setVariable("issueDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            // 2. Processa o HTML
            // Certifique-se de ter o arquivo src/main/resources/templates/certificate.html criado
            String htmlContent = templateEngine.process("certificate", context);

            // 3. Converte HTML para PDF
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do certificado", e);
        }
    }

    public Resource getCertificateFile(UUID certificateId) {
        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new BusinessRuleException("Certificado não encontrado."));
        return fileStorageService.loadAsResource(cert.getFilePath());
    }

    // =================================================================================
    // Classe Auxiliar Interna para converter byte[] em MultipartFile
    // =================================================================================
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String name;
        private final String contentType;

        public ByteArrayMultipartFile(byte[] content, String name, String contentType) {
            this.content = content;
            this.name = name;
            this.contentType = contentType;
        }

        @Override
        @NonNull
        public String getName() {
            return name;
        }

        @Override
        @NonNull
        public String getOriginalFilename() {
            return name;
        }

        @Override
        @NonNull
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        @NonNull
        public byte[] getBytes() {
            // Removido 'throws IOException' pois leitura de array em memória não gera erro de IO
            return content;
        }

        @Override
        @NonNull
        public InputStream getInputStream() {
            // Removido 'throws IOException'
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(@NonNull File dest) throws IOException, IllegalStateException {
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(content);
            }
        }
    }
}