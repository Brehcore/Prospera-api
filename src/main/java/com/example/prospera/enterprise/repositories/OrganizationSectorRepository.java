package com.example.prospera.enterprise.repositories;

import com.example.prospera.enterprise.domain.Organization;
import com.example.prospera.enterprise.domain.OrganizationSector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Deleta a "adoção" de um setor por uma organização,
     * baseado no ID da organização e no ID do setor.
     */
    @Transactional
    void deleteByOrganizationIdAndSectorId(UUID organizationId, UUID sectorId);

    /**
     * Verifica se existe alguma "adoção" para um dado sectorId.
     */
    boolean existsBySectorId(UUID sectorId);


}
