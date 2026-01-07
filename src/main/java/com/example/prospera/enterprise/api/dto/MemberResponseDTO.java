package com.example.prospera.enterprise.api.dto;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.auth.domain.UserProfilePF;
import com.example.prospera.common.enums.OrganizationRole;
import com.example.prospera.enterprise.domain.Membership;

import java.util.UUID;

/**
 * DTO para representar um membro de uma organização na resposta da API.
 * Contém dados formatados para exibição no frontend.
 */
public record MemberResponseDTO(
        UUID membershipId,
        UUID userId,
        String userEmail,
        String fullName, // O nome completo do usuário
        String role      // A role traduzida para "Administrador" ou "Membro"
) {
    public static MemberResponseDTO fromEntity(Membership membership) {
        AuthUser user = membership.getUser();
        UserProfilePF personalProfile = user.getPersonalProfile();

        // Lógica para pegar o nome completo:
        String name = (personalProfile != null && personalProfile.getFullName() != null)
                ? personalProfile.getFullName()
                : user.getEmail(); // Fallback para o email se não houver perfil

        // Lógica para traduzir a role
        String translatedRole = translateRole(membership.getRole());

        return new MemberResponseDTO(
                membership.getId(),
                user.getId(),
                user.getEmail(),
                name,
                translatedRole // Usa a role traduzida
        );
    }

    /**
     * Método auxiliar privado para traduzir o enum OrganizationRole para uma String amigável.
     */
    private static String translateRole(OrganizationRole role) {
        if (role == null) {
            return "Indefinido";
        }
        return switch (role) {
            case ORG_ADMIN -> "Administrador";
            case ORG_MEMBER -> "Membro";
        };
    }
}