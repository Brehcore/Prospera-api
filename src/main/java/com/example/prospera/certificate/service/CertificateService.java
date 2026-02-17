package com.example.prospera.certificate.service;

import com.example.prospera.auth.exceptions.BusinessRuleException;
import com.example.prospera.certificate.api.dto.CertificateListItemDTO;
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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FileStorageService fileStorageService;
    private final TemplateEngine templateEngine;
    private final ModuleRepository moduleRepository; // Para calcular horas de vídeo

    // Injeção dos valores do application.properties
    @org.springframework.beans.factory.annotation.Value("${app.company.name:Go-Tree Consultoria}")
    private String companyName;

    @org.springframework.beans.factory.annotation.Value("${app.company.cnpj:CNPJ não informado}")
    private String companyCnpj;

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

    @Transactional(readOnly = true)
    public List<CertificateListItemDTO> getMyCertificates(UUID userId, String search) {

        // Busca no banco com o filtro
        List<Certificate> certificates = certificateRepository.findMyCertificates(userId, search);

        // Converte para DTO
        return certificates.stream().map(cert -> {

            // Gera a URL de download dinâmica
            String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/certificates/download/")
                    .path(cert.getId().toString())
                    .toUriString();

            // Gera a URL da miniatura dinâmica
            String thumbnailUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/certificates/")
                    .path(cert.getId().toString())
                    .path("/thumbnail")
                    .toUriString();

            return new CertificateListItemDTO(
                    cert.getId(),
                    cert.getEnrollment().getTraining().getTitle(),
                    thumbnailUrl,
                    cert.getIssuedAt(),
                    cert.getWorkloadSnapshot(), // "10 horas"
                    cert.getValidationCode(),
                    downloadUrl
            );
        }).toList();
    }

    /**
     * Gera uma imagem (JPG) dinâmica do certificado para usar como miniatura.
     * Desenha o texto sobre a imagem de fundo padrão.
     */
    public byte[] generateCertificateThumbnail(UUID certificateId) {
        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new EntityNotFoundException("Certificado não encontrado."));
        Enrollment enrollment = cert.getEnrollment();

        try {
            Resource resource = new ClassPathResource("CERTIFICADO.png"); // Sua imagem corrigida
            BufferedImage originalImage = ImageIO.read(resource.getInputStream());

            // CORREÇÃO: Criar uma nova imagem RGB (sem transparência) para evitar erros no JPG
            BufferedImage image = new BufferedImage(
                    originalImage.getWidth(),
                    originalImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D g2d = image.createGraphics();

            // Pinta o fundo de branco antes de desenhar o PNG por cima
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());

            // Desenha a imagem original (CERTIFICADO.png) sobre o fundo branco
            g2d.drawImage(originalImage, 0, 0, null);

            // Configurações de qualidade de renderização de texto (anti-aliasing)
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // --- CONFIGURAÇÃO DE FONTES E CORES ---
            // Cor do texto (verde escuro da sua identidade visual)
            Color primaryColor = new Color(15, 76, 58); // #0f4c3a
            g2d.setColor(primaryColor);

            // Definindo fontes (usando Serif padrão do sistema para simplificar,
            // para usar a Montserrat aqui seria necessário carregar o .ttf também)
            Font fontText = new Font("Serif", Font.PLAIN, 40); // Texto comum
            Font fontBoldBig = new Font("Serif", Font.BOLD, 70);   // Nome do Aluno
            Font fontBoldMedium = new Font("Serif", Font.BOLD, 50); // Nome do Curso
            Font fontSmall = new Font("Serif", Font.PLAIN, 30); // Rodapé

            int imgWidth = image.getWidth();
            // int imgHeight = image.getHeight(); // Se precisar para cálculos verticais

            // --- DESENHANDO OS TEXTOS (Bloco Central) ---

            // "Certificamos que"
            drawCenteredString(g2d, "Certificamos que", imgWidth, 800, fontText);

            // NOME DO ALUNO
            String studentName = enrollment.getUser().getPersonalProfile().getFullName();
            drawCenteredString(g2d, studentName, imgWidth, 950, fontBoldBig);

            // "concluiu com êxito o treinamento"
            drawCenteredString(g2d, "concluiu com êxito o treinamento", imgWidth, 1100, fontText);

            // NOME DO CURSO
            drawCenteredString(g2d, enrollment.getTraining().getTitle(), imgWidth, 1250, fontBoldMedium);

            // --- RODAPÉ ---
            g2d.setFont(fontSmall);

            // Canto Inferior Esquerdo (CNPJ e Empresa)
            int footerY = 1900; // Altura aproximada do rodapé
            int leftMargin = 150;

            g2d.drawString("Oferecido pela " + companyName, leftMargin, footerY);
            g2d.drawString("CNPJ: " + companyCnpj, leftMargin, footerY + 50);

            // Centro/Direita (Carga horária e Data)
            // Usando o snapshot salvo no momento da emissão
            String workload = cert.getWorkloadSnapshot() != null ? cert.getWorkloadSnapshot() : "N/A";
            String date = cert.getIssuedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            drawCenteredString(g2d, "Carga Horária: " + workload, imgWidth, footerY, fontSmall);
            drawCenteredString(g2d, "Data de Conclusão: " + date, imgWidth, footerY + 50, fontSmall);

            // --- FINALIZAÇÃO ---
            g2d.dispose(); // Libera recursos gráficos

            // Converte a imagem alterada para array de bytes (JPG)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Usamos JPEG para a miniatura ficar mais leve
            ImageIO.write(image, "jpg", baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar miniatura do certificado", e);
        }
    }

    // Metodo auxiliar para centralizar texto horizontalmente
    private void drawCenteredString(Graphics2D g2d, String text, int width, int y, Font font) {
        g2d.setFont(font);
        FontMetrics metrics = g2d.getFontMetrics(font);
        int x = (width - metrics.stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
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
            context.setVariable("companyName", companyName); // Variável injetada do application.properties
            context.setVariable("companyCnpj", companyCnpj); // Variável injetada do application.properties

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