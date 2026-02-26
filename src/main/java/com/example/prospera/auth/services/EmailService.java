package com.example.prospera.auth.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine; // Reaproveitamos o motor do Thymeleaf

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.company.name:Go-Tree Consultoria}")
    private String companyName;

    // Remetente das propriedades
    @Value("${spring.mail.username}")
    private String senderEmail;

    /**
     * Envia o certificado em PDF como anexo.
     * A anotação @Async garante que a requisição HTTP do usuário não fique travada esperando o envio.
     */
    @Async
    public void sendCertificateEmail(String toEmail, String studentName, String courseName, byte[] pdfBytes, String fileName) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            // true = indica que é "multipart" (permite HTML e Anexos)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, companyName);
            helper.setTo(toEmail);
            helper.setSubject("Seu Certificado de Conclusão - " + courseName);

            // 1. Prepara as variáveis para o HTML (Thymeleaf)
            Context context = new Context();
            context.setVariable("studentName", studentName);
            context.setVariable("courseName", courseName);
            context.setVariable("companyName", companyName);

            // 2. Processa o HTML e define no corpo do e-mail
            String htmlContent = templateEngine.process("certificate-email", context);
            helper.setText(htmlContent, true); // true = indica que o conteúdo é HTML

            // 3. Adiciona o PDF em memória como anexo
            helper.addAttachment(fileName, new ByteArrayResource(pdfBytes));

            // 4. Dispara o e-mail
            emailSender.send(message);

            System.out.println("E-mail de certificado enviado com sucesso para: " + toEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            // Se der erro de rede/SMTP, logamos, mas não quebramos o sistema
            System.err.println("Falha ao enviar e-mail de certificado para " + toEmail + ": " + e.getMessage());
        }
    }

    @Async // Envia em segundo plano para não travar a requisição
    public void sendResetTokenEmail(String to, String token) {
        // Ajuste a URL para o endereço do seu Frontend
        String url = "http://localhost:4200/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(to);
        message.setSubject("Redefinição de Senha - Prospera");
        message.setText("Olá,\n\nRecebemos um pedido para redefinir sua senha.\n" +
                "Clique no link abaixo para criar uma nova senha:\n\n" + url + "\n\n" +
                "Se você não solicitou isso, ignore este e-mail.");

        emailSender.send(message);
    }

    @Async
    public void sendEmailChangeCode(String toOldEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail); // injetado via @Value
        message.setTo(toOldEmail);
        message.setSubject("Código de Verificação - Troca de E-mail");
        message.setText("Você solicitou a troca de e-mail da sua conta Prospera.\n\n" +
                "Seu código de verificação é: " + code + "\n\n" +
                "Este código expira em 15 minutos.\n" +
                "Se não foi você, entre em contato com o suporte imediatamente.");

        emailSender.send(message);
    }
}