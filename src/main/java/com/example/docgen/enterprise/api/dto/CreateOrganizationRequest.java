package com.example.docgen.enterprise.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

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