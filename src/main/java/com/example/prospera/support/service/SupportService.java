package com.example.prospera.support.service;

import com.example.prospera.email.service.EmailService;
import com.example.prospera.support.dto.SupportTicketRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupportService {

    private final EmailService emailService;

    public void processTicket(SupportTicketRequest request) {
        // Pega o e-mail de destino correto direto do Enum
        String targetEmail = request.subject().getTargetEmail();
        String subjectDescription = request.subject().getDescription();

        // Dispara o e-mail de forma assíncrona
        emailService.sendSupportTicket(
                targetEmail,
                request.name(),
                request.userEmail(),
                subjectDescription,
                request.message()
        );
    }
}