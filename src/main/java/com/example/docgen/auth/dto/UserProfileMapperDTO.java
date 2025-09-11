package com.example.docgen.auth.dto;

import com.example.docgen.auth.domain.UserProfilePF;
import com.example.docgen.auth.domain.UserProfilePJ;

public class UserProfileMapperDTO {

    /**
     * Converte uma entidade UserProfilePF em um DTO de resposta consolidado.
     */
    public static UserProfileResponseDTO fromPfEntity(UserProfilePF profile) {
        if (profile == null) {
            return null;
        }
        // Combina dados do AuthUser (identidade) com os do UserProfilePF (perfil)
        return new UserProfileResponseDTO(
                profile.getAuthUser().getId(),
                profile.getAuthUser().getEmail(),
                profile.getAuthUser().getRole().name(),
                profile.getUserType(),
                profile.getFullName(), // Vem do perfil
                profile.getCpf(),       // Vem do perfil
                profile.getPhone()      // Vem do perfil
        );
    }

    /**
     * Converte uma entidade UserProfilePJ em um DTO de resposta consolidado.
     */
    public static UserProfileResponseDTO fromPjEntity(UserProfilePJ profile) {
        if (profile == null) {
            return null;
        }
        // Combina dados do AuthUser (identidade) com os do UserProfilePJ (perfil)
        return new UserProfileResponseDTO(
                profile.getAuthUser().getId(),
                profile.getAuthUser().getEmail(),
                profile.getAuthUser().getRole().name(),
                profile.getUserType(),
                profile.getRazaoSocial(), // Vem do perfil
                profile.getCnpj(),        // Vem do perfil
                profile.getPhone()        // Vem do perfil
        );
    }
}