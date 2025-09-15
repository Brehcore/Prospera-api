package com.example.docgen.enterprise.admin.dto;

import com.example.docgen.enterprise.domain.enums.OrganizationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrgStatusRequest(
        @NotNull(message = "O novo status é obrigatório.")
        OrganizationStatus newStatus
) {
}