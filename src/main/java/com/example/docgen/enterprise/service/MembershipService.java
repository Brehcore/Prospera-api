package com.example.docgen.enterprise.service;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.repositories.AuthUserRepository;
import com.example.docgen.common.enums.OrganizationRole;
import com.example.docgen.enterprise.api.dto.AddMemberRequest;
import com.example.docgen.enterprise.domain.Membership;
import com.example.docgen.enterprise.domain.Organization;
import com.example.docgen.enterprise.repositories.MembershipRepository;
import com.example.docgen.enterprise.repositories.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final OrganizationRepository organizationRepository;
    private final AuthUserRepository authUserRepository;
    private static final Logger log = LoggerFactory.getLogger(MembershipService.class);

    // Classe auxiliar interna para agrupar as entidades validadas.
    private record ValidatedAdminContext(AuthUser admin, Organization org) {
    }

    /**
     * MÉTODO PRIVADO CENTRALIZADOR: Valida se o usuário é admin da organização
     * e retorna as entidades "vivas" (gerenciadas) para uso seguro nos métodos.
     * Isso elimina a repetição de código.
     */
    private ValidatedAdminContext validateAdminAndGetContext(AuthUser detachedAdmin, UUID organizationId) {
        AuthUser managedAdmin = authUserRepository.findById(detachedAdmin.getId())
                .orElseThrow(() -> new RuntimeException("Usuário administrador não encontrado."));
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organização não encontrada."));

        checkIfUserIsOrgAdmin(managedAdmin, organization);

        return new ValidatedAdminContext(managedAdmin, organization);
    }

    @Transactional
    public Membership addMemberToOrganization(AuthUser adminUser, UUID organizationId, AddMemberRequest dto) {
        ValidatedAdminContext context = validateAdminAndGetContext(adminUser, organizationId);

        AuthUser userToAdd = authUserRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Usuário a ser adicionado não encontrado: " + dto.email()));

        // Regra de negócio: Impede que um usuário seja adicionado duas vezes na mesma organização
        if (membershipRepository.existsByUserAndOrganization(userToAdd, context.org())) {
            throw new IllegalStateException("Este usuário já é membro desta organização.");
        }

        Membership newMembership = Membership.builder()
                .user(userToAdd)
                .organization(context.org())
                .role(dto.role())
                .build();
        return membershipRepository.save(newMembership);
    }

    @Transactional(readOnly = true)
    public List<Membership> listMembers(AuthUser currentUser, UUID organizationId) {
        ValidatedAdminContext context = validateAdminAndGetContext(currentUser, organizationId);
        return membershipRepository.findByOrganizationWithDetails(context.org());
    }

    @Transactional
    public void removeMember(AuthUser currentUser, UUID organizationId, UUID membershipId) {
        log.info("Iniciando remoção do membershipId: {}", membershipId);
        ValidatedAdminContext context = validateAdminAndGetContext(currentUser, organizationId);

        Membership membershipToRemove = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new RuntimeException("Afiliação (membership) não encontrada."));

        if (!membershipToRemove.getOrganization().equals(context.org())) {
            throw new AccessDeniedException("Esta afiliação não pertence à organização especificada.");
        }

        boolean isSelfRemoval = context.admin().equals(membershipToRemove.getUser());

        if (isSelfRemoval) {
            // Se o admin está tentando se remover, ele só pode se não for o último.
            log.debug("Detectada tentativa de auto-remoção pelo admin {}", context.admin().getEmail());
            if (isLastAdmin(context.org())) {
                throw new IllegalStateException("Você não pode sair da organização pois é o último administrador. Promova outro membro primeiro.");
            }
        } else {
            // Se está removendo outra pessoa, verifica se o alvo é o último admin.
            // (Esta regra impede um admin de, maliciosamente, remover o outro último admin)
            if (membershipToRemove.getRole() == OrganizationRole.ORG_ADMIN && isLastAdmin(context.org())) {
                throw new IllegalStateException("Não é possível remover o último administrador da organização.");
            }
        }

        log.info("Validações passaram. Deletando afiliação {}", membershipToRemove.getId());
        membershipRepository.delete(membershipToRemove);
        log.info("Afiliação {} removida com sucesso.", membershipToRemove.getId());
    }

    @Transactional
    public Membership updateMemberRole(AuthUser currentUser, UUID organizationId, UUID membershipId, OrganizationRole newRole) {
        ValidatedAdminContext context = validateAdminAndGetContext(currentUser, organizationId);

        Membership membershipToUpdate = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new RuntimeException("Afiliação (membership) não encontrada."));

        if (!membershipToUpdate.getOrganization().equals(context.org())) {
            throw new AccessDeniedException("Esta afiliação não pertence à organização especificada.");
        }

        if (membershipToUpdate.getUser().equals(context.admin()) &&
                newRole == OrganizationRole.ORG_MEMBER &&
                isLastAdmin(context.org())) {
            throw new IllegalStateException("Você não pode remover seu próprio acesso de administrador pois é o último da organização.");
        }

        membershipToUpdate.setRole(newRole);
        return membershipRepository.save(membershipToUpdate);
    }

    private void checkIfUserIsOrgAdmin(AuthUser user, Organization organization) {
        boolean isOrgAdmin = user.getMemberships().stream()
                .anyMatch(m -> m.getOrganization().equals(organization) && m.getRole() == OrganizationRole.ORG_ADMIN);
        if (!isOrgAdmin) {
            throw new AccessDeniedException("Acesso negado. Você não é administrador desta organização.");
        }
    }

    /**
     * MÉTODO isLastAdmin CORRIGIDO: Agora consulta o repositório diretamente
     * para uma contagem precisa, evitando problemas de lazy loading.
     */
    private boolean isLastAdmin(Organization organization) {
        return membershipRepository.countByOrganizationAndRole(organization, OrganizationRole.ORG_ADMIN) <= 1;
    }
}