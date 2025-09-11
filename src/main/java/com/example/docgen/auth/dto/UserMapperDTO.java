package com.example.docgen.auth.dto;

import com.example.docgen.auth.domain.AuthUser;

/**
 * Mapper responsável por converter a entidade de identidade (AuthUser)
 * para DTOs de resposta simples.
 */
public class UserMapperDTO {

    /**
     * Converte a entidade AuthUser para um UserResponseDTO.
     * Usado principalmente na resposta do endpoint de registro.
     *
     * @param user A entidade AuthUser que foi criada.
     * @return um DTO com os dados públicos da nova identidade.
     */
    public static UserResponseDTO toDto(AuthUser user) {
        if (user == null) {
            return null;
        }

        // Cria o UserResponseDTO usando os dados do AuthUser
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getRole().name() // Usa .name() para obter a String do enum
		);
	}
}