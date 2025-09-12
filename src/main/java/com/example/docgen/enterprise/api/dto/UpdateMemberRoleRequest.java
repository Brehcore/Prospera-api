package com.example.docgen.enterprise.api.dto;

import com.example.docgen.common.enums.OrganizationRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(
        @NotNull(message = "A nova função é obrigatória.")
        OrganizationRole newRole
) {
}