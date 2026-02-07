package com.example.prospera.certificate.service;

import com.example.prospera.auth.exceptions.BusinessRuleException;
import com.example.prospera.certificate.domain.Certificate;
import com.example.prospera.certificate.repositories.CertificateRepository;
import com.example.prospera.courses.domain.Enrollment;
import com.example.prospera.courses.domain.Training;
import com.example.prospera.courses.domain.enums.EnrollmentStatus;
import com.example.prospera.courses.domain.enums.TrainingEntityType;
import com.example.prospera.courses.repositories.EnrollmentRepository;
import com.example.prospera.courses.repositories.ModuleRepository;
import com.example.prospera.courses.service.FileStorageService;
import com.lowagie.text.pdf.BaseFont;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
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
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FileStorageService fileStorageService;
    private final TemplateEngine templateEngine;
    private final ModuleRepository moduleRepository; // Injetado para calcular horas de vídeo

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

        // 4. Gera Código de Validação
        String validationCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 5. Calcula a Carga Horária (Texto formatado, ex: "10 horas")
        String workloadText = calculateWorkloadText(enrollment.getTraining());

        // 6. Gera o PDF a partir do HTML
        byte[] pdfBytes = generatePdfFromTemplate(enrollment, validationCode, workloadText);

        // 7. Salva o arquivo físico
        String fileName = "certificate_" + enrollmentId + ".pdf";
        MultipartFile multipartFile = new ByteArrayMultipartFile(pdfBytes, fileName, "application/pdf");
        String storedPath = fileStorageService.save(multipartFile);

        // 8. Salva no Banco (com o snapshot da carga horária)
        Certificate cert = Certificate.builder()
                .enrollment(enrollment)
                .validationCode(validationCode)
                .filePath(storedPath)
                .workloadSnapshot(workloadText) // Importante: Salva o valor calculado
                .build();

        return certificateRepository.save(cert);
    }

    /**
     * Calcula a carga horária baseada no tipo de treinamento.
     * Arredonda sempre para cima para beneficiar o aluno.
     */
    private String calculateWorkloadText(Training training) {
        int totalMinutes = 0;

        if (training.getEntityType() == TrainingEntityType.EBOOK) {
            // Lógica Probabilística: 1 página = 3 minutos de leitura
            // Se pageCount for nulo, assume 0
            int pages = (training.getPageCount() != null) ? training.getPageCount() : 0;
            totalMinutes = pages * 3;

        } else if (training.getEntityType() == TrainingEntityType.RECORDED_COURSE) {
            // Lógica Exata: Soma a duração de todas as lições cadastradas no banco
            Integer dbMinutes = moduleRepository.calculateTotalDurationByTrainingId(training.getId());
            totalMinutes = (dbMinutes != null) ? dbMinutes : 0;
        }

        // Regra de Negócio: Arredondar para cima (Ceiling)
        // Ex: 61 minutos -> 2 horas.
        // Ex: 0 minutos -> 1 hora (mínimo padrão).
        int hours = (int) Math.ceil(totalMinutes / 60.0);
        if (hours < 1) hours = 1;

        return hours + " horas";
    }

    /**
     * Gera o PDF processando o template HTML com o Thymeleaf e convertendo com Flying Saucer.
     */
    private byte[] generatePdfFromTemplate(Enrollment enrollment, String validationCode, String workloadText) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 1. Contexto do Thymeleaf (Variáveis para o HTML)
            Context context = new Context();

            // Nome do aluno (Prefere do perfil, fallback para email)
            String studentName = (enrollment.getUser().getPersonalProfile() != null)
                    ? enrollment.getUser().getPersonalProfile().getFullName()
                    : enrollment.getUser().getEmail();

            context.setVariable("studentName", studentName);
            context.setVariable("courseName", enrollment.getTraining().getTitle()); // Ajustado para bater com seu HTML
            context.setVariable("workload", workloadText);
            context.setVariable("validationCode", validationCode);
            context.setVariable("completionDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))); // Ajustado nome da variável

            // 2. Carregar imagem de fundo e converter para Base64
            try {
                String bgImageBase64 = convertImageToBase64("CERTIFICADO.png");
                context.setVariable("bgImageBase64", bgImageBase64);
            } catch (IOException e) {
                // Se der erro na imagem, logamos mas não paramos o processo (o certificado sai sem fundo)
                System.err.println("Aviso: Imagem de fundo do certificado não encontrada.");
                context.setVariable("bgImageBase64", "");
            }

            // 3. Processa o HTML
            String htmlContent = templateEngine.process("certificate", context);

            // 4. Converte HTML para PDF
            ITextRenderer renderer = new ITextRenderer();

            try {
                renderer.getFontResolver().addFont(
                        new ClassPathResource("fonts/Montserrat.ttf").getURL().toExternalForm(),
                        BaseFont.IDENTITY_H,
                        BaseFont.EMBEDDED
                );
            } catch (IOException e) {
                // Logue o erro, mas não trave a aplicação se a fonte falhar (vai usar a padrão)
                System.err.println("Erro ao carregar fonte customizada: " + e.getMessage());
            }

            // Configuração para Imagens
            renderer.getSharedContext().setReplacedElementFactory(
                    new org.xhtmlrenderer.pdf.ITextReplacedElementFactory(renderer.getOutputDevice())
            );

            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do certificado", e);
        }
    }

    /**
     * Lê um arquivo da pasta resources e converte para String Base64.
     */
    private String convertImageToBase64(String fileName) throws IOException {
        Resource resource = new ClassPathResource(fileName);
        byte[] imageBytes = StreamUtils.copyToByteArray(resource.getInputStream());
        return Base64.getEncoder().encodeToString(imageBytes);
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
            return content;
        }

        @Override
        @NonNull
        public InputStream getInputStream() {
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