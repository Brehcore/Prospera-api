package com.example.docgen.enterprise.service;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.common.enums.OrganizationRole;
import com.example.docgen.enterprise.api.dto.SectorDTO;
import com.example.docgen.enterprise.domain.Membership;
import com.example.docgen.enterprise.domain.Organization;
import com.example.docgen.enterprise.domain.OrganizationSector;
import com.example.docgen.enterprise.domain.Sector;
import com.example.docgen.enterprise.domain.UserSector;
import com.example.docgen.enterprise.repositories.MembershipRepository;
import com.example.docgen.enterprise.repositories.OrganizationRepository;
import com.example.docgen.enterprise.repositories.OrganizationSectorRepository;
import com.example.docgen.enterprise.repositories.SectorRepository;
import com.example.docgen.enterprise.repositories.UserSectorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectorAssignmentService {

    private final OrganizationRepository organizationRepository;
    private final SectorRepository sectorRepository;
    private final OrganizationSectorRepository organizationSectorRepository;
    private final UserSectorRepository userSectorRepository;
    private final MembershipRepository membershipRepository;

    /**
     * Adiciona um setor do catálogo global a uma organização.
     * Ação normalmente executada por um ORG_ADMIN.
     *
     * @param orgId    o ID da organização
     * @param sectorId o ID do setor a ser adicionado
     */
    @Transactional
    public void addSectorToOrganization(UUID orgId, UUID sectorId) {
        // Validação 1: A organização existe?
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada com o ID: " + orgId));

        // Validação 2: O setor existe no catálogo global?
        if (!sectorRepository.existsById(sectorId)) {
            throw new EntityNotFoundException("Setor não encontrado com o ID: " + sectorId);
        }

        // Validação 3: A organização já não possui este setor?
        if (organizationSectorRepository.existsByOrganizationAndSectorId(organization, sectorId)) {
            throw new IllegalStateException("A organização já possui este setor.");
        }

        OrganizationSector orgSector = new OrganizationSector(organization, sectorId);
        organizationSectorRepository.save(orgSector);
    }

    /**
     * Atribui um setor (já adotado pela organização) a um membro.
     * Ação executada por um ORG_ADMIN.
     *
     * @param adminUser      o usuário administrador autenticado
     * @param organizationId o ID da organização do contexto (da URL)
     * @param membershipId   o ID da afiliação do membro que receberá o setor
     * @param sectorId       o ID do setor a ser atribuído
     */
    @Transactional
    public void assignSectorToMember(AuthUser adminUser, UUID organizationId, UUID membershipId, UUID sectorId) {
        // Validação 1: O adminUser logado é de fato um admin da organização em questão?
        boolean isAdminOfOrg = adminUser.getMemberships().stream()
                .anyMatch(m -> m.getOrganization().getId().equals(organizationId) && m.getRole() == OrganizationRole.ORG_ADMIN);
        if (!isAdminOfOrg) {
            throw new AccessDeniedException("Acesso negado. O usuário logado não é administrador desta organização.");
        }

        // Validação 2: A afiliação (membership) do membro alvo existe?
        Membership targetMembership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Afiliação (Membro) não encontrada com o ID: " + membershipId));

        // Validação 3: O membro alvo realmente pertence à organização informada na URL?
        if (!targetMembership.getOrganization().getId().equals(organizationId)) {
            throw new AccessDeniedException("Conflito de dados. O membro não pertence à organização especificada.");
        }

        // Validação 4: O setor a ser atribuído existe no catálogo global?
        if (!sectorRepository.existsById(sectorId)) {
            throw new EntityNotFoundException("Setor não encontrado com o ID: " + sectorId);
        }

        // Validação 5: A organização já "adotou" este setor?
        if (!organizationSectorRepository.existsByOrganizationIdAndSectorId(organizationId, sectorId)) {
            throw new IllegalStateException("A organização não adotou este setor. Adicione o setor à organização primeiro.");
        }

        UUID memberUserId = targetMembership.getUser().getId();

        // Validação 6: O membro já não possui este setor atribuído?
        if (userSectorRepository.existsByUserIdAndSectorId(memberUserId, sectorId)) {
            throw new IllegalStateException("O membro já possui este setor atribuído.");
        }

        // Se todas as validações passaram, cria a associação
        UserSector userSector = new UserSector(memberUserId, sectorId, organizationId);
        userSectorRepository.save(userSector);
    }

    /**
     * Atribui um setor de interesse a um usuário pessoa física (sem organização).
     *
     * @param userId   o ID do usuário
     * @param sectorId o ID do setor de interesse
     */
    @Transactional
    public void assignSectorToPf(UUID userId, UUID sectorId) {
        // Validação 1: O setor existe no catálogo global?
        if (!sectorRepository.existsById(sectorId)) {
            throw new EntityNotFoundException("Setor não encontrado com o ID: " + sectorId);
        }

        // Validação 2: O usuário já não escolheu este setor?
        if (userSectorRepository.existsByUserIdAndSectorId(userId, sectorId)) {
            throw new IllegalStateException("O usuário já possui este setor de interesse.");
        }

        // Cria a associação com organizationId nulo
        UserSector userSector = new UserSector(userId, sectorId, null);
        userSectorRepository.save(userSector);
    }

    /**
     * Busca e retorna todos os setores que foram "adotados" por uma organização específica.
     */
    @Transactional(readOnly = true)
    public List<SectorDTO> getSectorsForOrganization(UUID organizationId) {
        // 1. Usa o novo método do repositório para encontrar todos os vínculos da organização com setores.
        List<OrganizationSector> sectorAssignments = organizationSectorRepository.findAllByOrganizationId(organizationId);

        // 2. Se não houver setores associados, retorna uma lista vazia.
        if (sectorAssignments.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. Extrai apenas os IDs dos setores a partir dos vínculos encontrados.
        List<UUID> sectorIds = sectorAssignments.stream()
                .map(OrganizationSector::getSectorId)
                .collect(Collectors.toList());

        // 4. Busca no repositório de setores todos os setores correspondentes a esses IDs.
        List<Sector> sectors = sectorRepository.findAllById(sectorIds);

        // 5. Converte a lista de entidades 'Sector' para uma lista de 'SectorDTO' e a retorna.
        return sectors.stream()
                .map(SectorDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Remove a "adoção" de um setor por uma organização.
     */
    @Transactional
    public void removeSectorFromOrganization(UUID organizationId, UUID sectorId) {
        // A validação de que o ORG_ADMIN tem permissão para a organizationId
        // já foi feita no controller.

        // Chama o novo método do repositório para executar a exclusão.
        organizationSectorRepository.deleteByOrganizationIdAndSectorId(organizationId, sectorId);
    }
}