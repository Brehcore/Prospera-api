package com.example.docgen.auth.admin.dto;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.common.enums.UserRole;

import java.util.UUID;

// DTO para a visão resumida na lista de usuários
public record AdminUserSummaryDTO(
        UUID id,
        String email,
        UserRole role,
        boolean enabled
) {
    public static AdminUserSummaryDTO fromEntity(AuthUser user) {
        return new AdminUserSummaryDTO(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled()
        );
    }
}