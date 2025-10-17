package com.example.docgen.enterprise.api.dto;

import com.example.docgen.enterprise.domain.Organization;

import java.util.UUID;

/**
 * DTO para a resposta da criação ou consulta de uma Organização.
 */
public record OrganizationResponseDTO(
        UUID id,
        String razaoSocial,
        String cnpj
) {
    /**
     * Metodo de fábrica para converter uma entidade Organization para este DTO.
     */
    public static OrganizationResponseDTO fromEntity(Organization organization) {
        return new OrganizationResponseDTO(
                organization.getId(),
                organization.getRazaoSocial(),
                organization.getCnpj()
        );
    }
}