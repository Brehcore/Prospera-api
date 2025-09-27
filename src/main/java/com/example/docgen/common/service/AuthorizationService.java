package com.example.docgen.common.service;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.common.enums.OrganizationRole;
import com.example.docgen.enterprise.domain.Membership;
import com.example.docgen.enterprise.repositories.MembershipRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

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
            throw new AccessDeniedException("Acesso negado. Você не é administrador desta organização.");
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
        // A mesma lógica que estava duplicada, agora em um só lugar.
        boolean isMember = user.getMemberships().stream()
                .anyMatch(m -> m.getOrganization().getId().equals(organizationId));
        if (!isMember) {
            // Mensagem de erro genérica que serve para vários contextos
            throw new AccessDeniedException("O usuário não é membro da organização especificada.");
        }
    }
}