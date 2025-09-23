package com.example.docgen.enterprise.repositories;

import com.example.docgen.enterprise.domain.Organization;
import com.example.docgen.enterprise.domain.OrganizationSector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrganizationSectorRepository extends JpaRepository<OrganizationSector, UUID> {
    List<OrganizationSector> findByOrganizationId(UUID organizationId);

    boolean existsByOrganizationAndSectorId(Organization organization, UUID sectorId);

    /**
     * O Spring Data JPA entende pelo nome "existsByOrganizationIdAndSectorId"
     * que deve criar uma query que verifica a existência de um registro
     * com base no ID da organização e no ID do setor.
     */
    boolean existsByOrganizationIdAndSectorId(UUID organizationId, UUID sectorId);

    /**
     * Encontra todas as associações de setores para um ID de organização específico.
     */
    List<OrganizationSector> findAllByOrganizationId(UUID organizationId);

}
