package com.example.prospera.auth.dto;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.auth.domain.UserProfilePF;
import com.example.prospera.common.enums.UserType;

/**
 * Mapper responsável por converter a entidade de perfil PF
 * em um DTO de resposta para a API.
 */
public class UserProfileMapperDTO {

    /**
     * Converte uma entidade UserProfilePF em um UserProfileResponseDTO consolidado.
     * Combina dados do AuthUser (identidade) com os do UserProfilePF (perfil).
     */
    public static UserProfileResponseDTO fromPfEntity(UserProfilePF profile) {
        if (profile == null) {
            return null;
        }

        AuthUser user = profile.getUser();

        // O retorno aqui usa o UserProfileResponseDTO
        return new UserProfileResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                UserType.PF, // Assumindo que você tenha o enum UserType
                profile.getFullName(),
                profile.getCpf(),
                profile.getPhone()
        );
    }
}