package com.example.prospera.enterprise.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para a requisição de criação de uma nova organização (PJ).
 */
public record CreateOrganizationRequest(
        @NotBlank(message = "A razão social é obrigatória")
        String razaoSocial,

        @NotBlank(message = "O CNPJ é obrigatório")
        String cnpj
) {
}