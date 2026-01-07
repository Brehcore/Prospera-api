package com.example.prospera.enterprise.admin;

import com.example.prospera.enterprise.api.dto.SectorDTO;
import com.example.prospera.enterprise.domain.Organization;
import com.example.prospera.enterprise.domain.OrganizationSector;
import com.example.prospera.enterprise.domain.Sector;
import com.example.prospera.enterprise.domain.enums.OrganizationStatus;
import com.example.prospera.enterprise.repositories.OrganizationRepository;
import com.example.prospera.enterprise.repositories.OrganizationSectorRepository;
import com.example.prospera.enterprise.repositories.SectorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOrganizationService {

    private final OrganizationRepository organizationRepository;
    // CORREÇÃO: Injetar os repositórios que faltavam.
    private final OrganizationSectorRepository organizationSectorRepository;
    private final SectorRepository sectorRepository;

    @Transactional(readOnly = true)
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll(Sort.by("razaoSocial"));
    }

    @Transactional(readOnly = true)
    public Organization getOrganizationDetails(UUID organizationId) {
        return organizationRepository.findWithMembersById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada: " + organizationId));
    }

    @Transactional
    public Organization updateOrganizationStatus(UUID organizationId, OrganizationStatus newStatus) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada: " + organizationId));
        organization.setStatus(newStatus);
        return organizationRepository.save(organization);
    }

    @Transactional(readOnly = true)
    public List<SectorDTO> getSectorsForOrganization(UUID organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new EntityNotFoundException("Organização não encontrada: " + organizationId);
        }
        List<OrganizationSector> orgSectors = organizationSectorRepository.findByOrganizationId(organizationId);

        // CORREÇÃO: Lógica completada para buscar os detalhes dos setores e converter para DTO.
        if (orgSectors.isEmpty()) {
            return List.of();
        }
        List<UUID> sectorIds = orgSectors.stream().map(OrganizationSector::getSectorId).toList();
        List<Sector> sectors = sectorRepository.findAllById(sectorIds);

        return sectors.stream().map(SectorDTO::fromEntity).collect(Collectors.toList());
    }
}