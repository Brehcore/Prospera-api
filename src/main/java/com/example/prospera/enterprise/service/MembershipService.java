package com.example.prospera.enterprise.service;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.auth.repositories.AuthUserRepository;
import com.example.prospera.common.enums.OrganizationRole;
import com.example.prospera.common.service.AuthorizationService;
import com.example.prospera.courses.dto.EnrollmentResponseDTO;
import com.example.prospera.courses.service.EnrollmentService;
import com.example.prospera.enterprise.domain.Membership;
import com.example.prospera.enterprise.domain.Organization;
import com.example.prospera.enterprise.domain.Sector;
import com.example.prospera.enterprise.dto.AddMemberRequest;
import com.example.prospera.enterprise.dto.MemberDetailDTO;
import com.example.prospera.enterprise.repositories.MembershipRepository;
import com.example.prospera.enterprise.repositories.OrganizationRepository;
import com.example.prospera.enterprise.repositories.UserSectorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipService {

    // Dependências agora mais enxutas
    private final MembershipRepository membershipRepository;
    private final OrganizationRepository organizationRepository;
    private final AuthUserRepository authUserRepository;
    private final UserSectorRepository userSectorRepository;
    private final AuthorizationService authorizationService;
    private final EnrollmentService enrollmentService;


    private static final Logger log = LoggerFactory.getLogger(MembershipService.class);


    @Transactional
    public Membership addMemberToOrganization(AuthUser adminUser, UUID organizationId, AddMemberRequest dto) {
        // 1. Validação de permissão primeiro
        authorizationService.checkIsOrgAdmin(adminUser, organizationId);

        // 2. Busca das entidades necessárias para a lógica de negócio
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada."));
        AuthUser userToAdd = authUserRepository.findByEmail(dto.email())
                .orElseThrow(() -> new EntityNotFoundException("Usuário a ser adicionado não encontrado: " + dto.email()));

        if (membershipRepository.existsByUserAndOrganization(userToAdd, organization)) {
            throw new IllegalStateException("Este usuário já é membro desta organização.");
        }

        Membership newMembership = Membership.builder()
                .user(userToAdd)
                .organization(organization)
                .role(dto.role())
                .addedBy(adminUser) // O adminUser do controllers já é a entidade correta
                .build();
        return membershipRepository.save(newMembership);
    }

    @Transactional(readOnly = true)
    public List<Membership> listMembers(AuthUser currentUser, UUID organizationId) {
        authorizationService.checkIsOrgAdmin(currentUser, organizationId);
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada."));
        return membershipRepository.findByOrganizationWithDetails(organization);
    }

    @Transactional
    public void removeMember(AuthUser currentUser, UUID organizationId, UUID membershipId) {
        log.info("Iniciando remoção do membershipId: {}", membershipId);
        authorizationService.checkIsOrgAdmin(currentUser, organizationId); // Validação delegada
        Membership membershipToRemove = authorizationService.checkMembershipBelongsToOrg(membershipId, organizationId); // Validação delegada

        boolean isSelfRemoval = membershipToRemove.getUser().getId().equals(currentUser.getId());

        if (isSelfRemoval) {
            if (isLastAdmin(membershipToRemove.getOrganization())) {
                throw new IllegalStateException("Você não pode sair da organização pois é o último administrador. Promova outro membro primeiro.");
            }
        } else {
            if (membershipToRemove.getRole() == OrganizationRole.ORG_ADMIN && isLastAdmin(membershipToRemove.getOrganization())) {
                throw new IllegalStateException("Não é possível remover o último administrador da organização.");
            }
        }

        log.info("Validações passaram. Deletando afiliação {}", membershipToRemove.getId());
        membershipRepository.delete(membershipToRemove);
        log.info("Afiliação {} removida com sucesso.", membershipToRemove.getId());
    }

    @Transactional
    public Membership updateMemberRole(AuthUser currentUser, UUID organizationId, UUID membershipId, OrganizationRole newRole) {
        authorizationService.checkIsOrgAdmin(currentUser, organizationId); // Validação delegada
        Membership membershipToUpdate = authorizationService.checkMembershipBelongsToOrg(membershipId, organizationId); // Validação delegada

        boolean isSelfDemotion = membershipToUpdate.getUser().getId().equals(currentUser.getId());

        if (isSelfDemotion && newRole == OrganizationRole.ORG_MEMBER && isLastAdmin(membershipToUpdate.getOrganization())) {
            throw new IllegalStateException("Você не pode remover seu próprio acesso de administrador pois é o último da organização.");
        }

        membershipToUpdate.setRole(newRole);
        return membershipRepository.save(membershipToUpdate);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getMemberEnrollmentProgress(AuthUser adminUser, UUID organizationId, UUID membershipId) {
        // 1. Valida se o admin tem permissão para acessar os dados deste membro.
        authorizationService.checkIsOrgAdmin(adminUser, organizationId);
        Membership targetMembership = authorizationService.checkMembershipBelongsToOrg(membershipId, organizationId);

        // 2. Obtém o usuário alvo a partir da afiliação validada.
        AuthUser targetUser = targetMembership.getUser();

        // 3. DELEGA a tarefa para o EnrollmentService, que é o especialista.
        //    Não há mais código duplicado.
        return enrollmentService.findEnrollmentsForUser(targetUser);
    }

    @Transactional(readOnly = true)
    public MemberDetailDTO getMemberDetails(AuthUser adminUser, UUID organizationId, UUID membershipId) {
        authorizationService.checkIsOrgAdmin(adminUser, organizationId); // Validação delegada
        Membership membership = authorizationService.checkMembershipBelongsToOrg(membershipId, organizationId);

        membership = membershipRepository.findWithDetailsById(membership.getId())
                .orElseThrow(() -> new EntityNotFoundException("Detalhes da afiliação não encontrados."));
        List<Sector> sectors = userSectorRepository.findSectorsByUserId(membership.getUser().getId());

        return MemberDetailDTO.fromEntity(membership, sectors);
    }

    // REGRA DE NEGÓCIO de Membership
    private boolean isLastAdmin(Organization organization) {
        return membershipRepository.countByOrganizationAndRole(organization, OrganizationRole.ORG_ADMIN) <= 1;
    }
}