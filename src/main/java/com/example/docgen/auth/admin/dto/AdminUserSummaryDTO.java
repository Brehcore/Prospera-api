package com.example.docgen.auth.admin.dto;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.domain.UserProfilePF;
import com.example.docgen.common.enums.UserRole;

import java.util.UUID;

// DTO para a visão resumida na lista de usuários
public record AdminUserSummaryDTO(
        UUID id,
        String name,
        String email,
        UserRole role,
        boolean enabled
) {
    public static AdminUserSummaryDTO fromEntity(AuthUser user) {
        UserProfilePF profile = user.getPersonalProfile();

        String displayName = (profile != null) ? profile.getFullName() : null;

        return new AdminUserSummaryDTO(
                user.getId(),
                displayName,
                user.getEmail(),
                user.getRole(),
                user.isEnabled()
        );
    }
}