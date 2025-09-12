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

    @Transactional
    public Membership addMemberToOrganization(AuthUser adminUser, UUID organizationId, AddMemberRequest dto) {
        // Busca a versão "viva" do administrador que está executando a ação.
        AuthUser managedAdminUser = authUserRepository.findById(adminUser.getId()) // Usando 'adminUser.getId()'
                .orElseThrow(() -> new RuntimeException("Usuário administrador não encontrado."));

        // Verifica se a organização existe
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organização não encontrada."));

        // Garante que o usuário que está fazendo a ação é um ADMIN desta organização
        checkIfUserIsOrgAdmin(managedAdminUser, organization);

        // Encontra o usuário a ser adicionado pelo email
        AuthUser userToAdd = authUserRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Usuário a ser adicionado não encontrado pelo email: " + dto.email()));

        // Cria e salva a nova afiliação (membership)
        Membership newMembership = Membership.builder()
                .user(userToAdd)
                .organization(organization)
                .role(dto.role())
                .build();


        return membershipRepository.save(newMembership);
    }

    @Transactional(readOnly = true)
// O método agora retorna a entidade
    public List<Membership> listMembers(AuthUser detachedCurrentUser, UUID organizationId) {
        AuthUser managedCurrentUser = authUserRepository.findById(detachedCurrentUser.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organização não encontrada."));

        checkIfUserIsOrgAdmin(managedCurrentUser, organization);

        // Retorna a lista de entidades diretamente
        return membershipRepository.findByOrganizationWithDetails(organization);
    }

    // --- NOVO MÉTODO PARA REMOVER UM MEMBRO ---
    @Transactional
    public void removeMember(AuthUser detachedCurrentUser, UUID organizationId, UUID membershipId) {
        // 1. Busca a versão "viva" do usuário que está executando a ação.
        AuthUser managedCurrentUser = authUserRepository.findById(detachedCurrentUser.getId())
                .orElseThrow(() -> new RuntimeException("Usuário administrador não encontrado."));

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organização não encontrada."));

        // 2. Segurança: Garante que o usuário atual é um admin desta organização.
        checkIfUserIsOrgAdmin(managedCurrentUser, organization);

        // 3. CORREÇÃO: Busca a afiliação a ser removida DIRETAMENTE pelo seu ID.
        Membership membershipToRemove = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new RuntimeException("Afiliação (membership) não encontrada."));

        // Validação de segurança extra: garante que a afiliação pertence à organização correta.
        if (!membershipToRemove.getOrganization().getId().equals(organizationId)) {
            throw new AccessDeniedException("Esta afiliação não pertence à organização especificada.");
        }

        // Verifica se o membro a ser removido é um admin
        if (membershipToRemove.getRole() == OrganizationRole.ORG_ADMIN) {
            // Se for um admin, verifica se é o último
            if (isLastAdmin(organization)) {
                throw new IllegalStateException("Não é possível remover o último administrador. Promova outro membro primeiro.");
            }
        }

        // 5. Deleta a afiliação encontrada.
        membershipRepository.delete(membershipToRemove);
    }

    /**
     * Método auxiliar de segurança para verificar se um usuário é admin de uma organização.
     */
    private void checkIfUserIsOrgAdmin(AuthUser user, Organization organization) {
        boolean isOrgAdmin = user.getMemberships().stream()
                .anyMatch(membership ->
                        membership.getOrganization().equals(organization) &&
                                membership.getRole() == OrganizationRole.ORG_ADMIN
                );

        if (!isOrgAdmin) {
            throw new AccessDeniedException("Acesso negado. Você não é administrador desta organização.");
        }
    }

    @Transactional
    public Membership updateMemberRole(AuthUser currentUser, UUID organizationId, UUID membershipId, OrganizationRole newRole) {
        // CORREÇÃO: Busca a versão "viva" do usuário que está fazendo a ação
        AuthUser managedCurrentUser = authUserRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Usuário administrador não encontrado."));

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organização não encontrada."));

        // Segurança: Agora usa a versão gerenciada
        checkIfUserIsOrgAdmin(managedCurrentUser, organization);

        Membership membershipToUpdate = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new RuntimeException("Afiliação (membership) não encontrada."));

        if (!membershipToUpdate.getOrganization().equals(organization)) {
            throw new AccessDeniedException("Esta afiliação não pertence à organização especificada.");
        }

        // A regra de negócio agora usa a versão gerenciada
        if (membershipToUpdate.getUser().equals(managedCurrentUser) &&
                newRole == OrganizationRole.ORG_MEMBER &&
                isLastAdmin(organization)) {
            throw new IllegalStateException("Você não pode remover seu próprio acesso de administrador pois é o último da organização.");
        }

        membershipToUpdate.setRole(newRole);
        return membershipRepository.save(membershipToUpdate);
    }

    /**
     * Método auxiliar para verificar se uma organização tem apenas um administrador.
     */
    private boolean isLastAdmin(Organization organization) {
        long adminCount = organization.getMemberships().stream()
                .filter(m -> m.getRole() == OrganizationRole.ORG_ADMIN)
                .count();
        return adminCount <= 1;
    }
}