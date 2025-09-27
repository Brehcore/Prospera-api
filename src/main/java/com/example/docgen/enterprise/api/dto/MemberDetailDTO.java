package com.example.docgen.enterprise.api.dto;

import com.example.docgen.enterprise.domain.Membership;
import com.example.docgen.enterprise.domain.Sector;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public record MemberDetailDTO(
        UUID membershipId,
        UUID userId,
        String userEmail,
        String fullName,
        String role,
        OffsetDateTime joinedAt,
        String addedByAdminEmail, // Quem o adicionou
        List<SectorDTO> assignedSectors // Lista de setores
) {
    /**
     * LÓGICA DE CONVERSÃO COMPLETA:
     * Converte uma entidade Membership e uma lista de Sectors para o DTO detalhado.
     */
    public static MemberDetailDTO fromEntity(Membership membership, List<Sector> sectors) {
        String adminEmail = Optional.ofNullable(membership.getAddedBy())
                .map(admin -> admin.getEmail())
                .orElse(null);

        String memberFullName = Optional.ofNullable(membership.getUser())
                .map(user -> user.getPersonalProfile())
                .map(profile -> profile.getFullName())
                .orElse("Nome não disponível");

        List<SectorDTO> sectorDTOs = Optional.ofNullable(sectors)
                .orElse(Collections.emptyList())
                .stream()
                .map(SectorDTO::fromEntity)
                .collect(Collectors.toList());

        return new MemberDetailDTO(
                membership.getId(),
                membership.getUser().getId(),
                membership.getUser().getEmail(),
                memberFullName,
                membership.getRole().name(), // Ou um nome mais amigável
                membership.getCreatedAt(), // Supondo que você tenha um campo 'createdAt' na entidade Membership
                adminEmail,
                sectorDTOs
        );
    }
}

