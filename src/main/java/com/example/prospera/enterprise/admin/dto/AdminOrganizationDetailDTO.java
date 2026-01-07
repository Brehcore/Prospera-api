package com.example.prospera.enterprise.admin.dto;

import com.example.prospera.common.enums.OrganizationRole;
import com.example.prospera.enterprise.domain.Organization;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// DTO para a visão completa de uma organização
public record AdminOrganizationDetailDTO(
        UUID id,
        String razaoSocial,
        String cnpj,
        List<MemberInfo> members
) {
    public static AdminOrganizationDetailDTO fromEntity(Organization org) {
        List<MemberInfo> memberInfo = (org.getMemberships() != null)
                ? org.getMemberships().stream().map(MemberInfo::fromEntity).collect(Collectors.toList())
                : Collections.emptyList();

        return new AdminOrganizationDetailDTO(
                org.getId(),
                org.getRazaoSocial(),
                org.getCnpj(),
                memberInfo
        );
    }

    // Sub-DTO para listar os membros dentro da organização
    public record MemberInfo(UUID userId, String userEmail, OrganizationRole role) {
        public static MemberInfo fromEntity(com.example.prospera.enterprise.domain.Membership m) {
            return new MemberInfo(m.getUser().getId(), m.getUser().getEmail(), m.getRole());
        }
    }
}