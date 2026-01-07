package com.example.prospera.common.service;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.common.enums.OrganizationRole;
import com.example.prospera.enterprise.domain.Membership;
import com.example.prospera.enterprise.repositories.MembershipRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final MembershipRepository membershipRepository;

    /**
     * Verifica se um usuário é um ORG_ADMIN de uma organização específica.
     * Lança AccessDeniedException se não for.
     */
    public void checkIsOrgAdmin(AuthUser user, UUID organizationId) {
        boolean isOrgAdmin = user.getMemberships().stream()
                .anyMatch(m -> m.getOrganization().getId().equals(organizationId)
                        && m.getRole() == OrganizationRole.ORG_ADMIN);
        if (!isOrgAdmin) {
            throw new AccessDeniedException("Acesso negado. Você não é administrador desta organização.");
        }
    }

    /**
     * Verifica se um membro (identificado por membershipId) pertence a uma organização.
     * Lança AccessDeniedException se não pertencer.
     * Retorna a entidade Membership se a verificação passar.
     */
    public Membership checkMembershipBelongsToOrg(UUID membershipId, UUID organizationId) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Afiliação (Membro) não encontrada com o ID: " + membershipId));

        if (!membership.getOrganization().getId().equals(organizationId)) {
            throw new AccessDeniedException("Conflito de dados. O membro não pertence à organização especificada.");
        }
        return membership;
    }

    /**
     * Verifica se um usuário é membro de uma organização específica.
     * Lança AccessDeniedException se não for.
     */
    public void checkIsMemberOfOrg(AuthUser user, UUID organizationId) {
        boolean isMember = user.getMemberships().stream()
                .anyMatch(m -> m.getOrganization().getId().equals(organizationId));
        if (!isMember) {
            throw new AccessDeniedException("O usuário não é membro da organização especificada.");
        }
    }

    // =============================================================
    // NOVO METODO (A FUNCIONALIDADE QUE ESTAVA FALTANDO)
    // =============================================================

    /**
     * Verifica se um usuário é um ORG_ADMIN de QUALQUER organização
     * dentro de uma Account específica.
     * Lança AccessDeniedException se a verificação falhar.
     *
     * @param user      O usuário cujas permissões estão sendo verificadas.
     * @param accountId O ID da Account a ser gerenciada.
     */
    @Transactional(readOnly = true)
    public void checkIsAdminOfAccount(AuthUser user, UUID accountId) {
        if (user.getMemberships() == null || user.getMemberships().isEmpty()) {
            throw new AccessDeniedException("Acesso negado. O usuário não é membro de nenhuma organização.");
        }

        boolean hasAdminRightsForAccount = user.getMemberships().stream()
                .anyMatch(membership ->
                        // A organização da filiação pertence à conta alvo?
                        membership.getOrganization().getAccount().getId().equals(accountId) &&
                                // E o papel do usuário é de administrador?
                                membership.getRole() == OrganizationRole.ORG_ADMIN
                );

        if (!hasAdminRightsForAccount) {
            throw new AccessDeniedException("Acesso negado. O usuário не possui permissão de administrador para esta conta.");
        }
    }
}