package com.example.prospera.enterprise.api.dto;

import com.example.prospera.common.enums.OrganizationRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(
        @NotNull(message = "A nova função é obrigatória.")
        OrganizationRole newRole
) {
}