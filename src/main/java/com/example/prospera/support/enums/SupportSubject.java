package com.example.prospera.support.enums;

import lombok.Getter;

@Getter
public enum SupportSubject {
    ACCESS_DIFFICULTIES("Dificuldades de acesso", "ti@gotreeconsultoria.com.br"),
    CONTENT_CERTIFICATIONS("Conteúdos e Certificações", "contato@gotreeconsultoria.com.br"),
    FINANCIAL_BILLING("Financeiro e Faturamento", "contato@gotreeconsultoria.com.br"),
    PLATFORM_INSTABILITY("Instabilidade na Plataforma", "ti@gotreeconsultoria.com.br"),
    OTHER("Outros assuntos", "contato@gotreeconsultoria.com.br");

    private final String description;
    private final String targetEmail;

    SupportSubject(String description, String targetEmail) {
        this.description = description;
        this.targetEmail = targetEmail;
    }
}