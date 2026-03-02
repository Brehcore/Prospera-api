package com.example.prospera.support.dto;

import com.example.prospera.support.enums.SupportSubject;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SupportTicketRequest(
        @NotBlank(message = "O nome é obrigatório")
        String name,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String userEmail,

        @NotNull(message = "O assunto do chamado é obrigatório")
        SupportSubject subject,

        @NotBlank(message = "A mensagem não pode estar vazia")
        String message
) {
}