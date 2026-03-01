package com.example.prospera.admin.dto;

import com.example.prospera.enterprise.domain.enums.OrganizationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrgStatusRequest(
        @NotNull(message = "O novo status é obrigatório.")
        OrganizationStatus newStatus
) {
}