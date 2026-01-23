package com.example.prospera.auth.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;

    // Remetente das propriedades
    @Value("${spring.mail.username}")
    private String senderEmail;

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