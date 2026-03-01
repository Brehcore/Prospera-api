package com.example.prospera.enterprise.dto;

import com.example.prospera.common.enums.OrganizationRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(
        @NotBlank(message = "O email do membro é obrigatório")
        @Email
        String email,

        @NotNull(message = "A role do membro é obrigatória")
        OrganizationRole role // Ex: "ORG_MEMBER" ou "ORG_ADMIN"
) {
}