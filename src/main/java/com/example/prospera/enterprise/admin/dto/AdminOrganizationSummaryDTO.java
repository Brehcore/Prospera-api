package com.example.prospera.enterprise.admin.dto;

import com.example.prospera.enterprise.domain.Organization;
import com.example.prospera.enterprise.domain.enums.OrganizationStatus;

import java.util.UUID;

// DTO para a visão resumida na lista de organizações
public record AdminOrganizationSummaryDTO(
        UUID id,
        String razaoSocial,
        String cnpj,
        int memberCount, // Adicionamos a contagem de membros para uma visão rápida
        OrganizationStatus status
) {
    public static AdminOrganizationSummaryDTO fromEntity(Organization org) {
        return new AdminOrganizationSummaryDTO(
                org.getId(),
                org.getRazaoSocial(),
                org.getCnpj(),
                org.getMemberships() != null ? org.getMemberships().size() : 0,
                org.getStatus()
        );
    }
}