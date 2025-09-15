package com.example.docgen.auth.services;

import com.example.docgen.auth.api.dto.ProfileMeResponseDTO;
import com.example.docgen.auth.api.dto.UserProfilePFRequest;
import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.domain.UserProfilePF;
import com.example.docgen.auth.repositories.AuthUserRepository;
import com.example.docgen.auth.repositories.UserProfilePfRepository;
import com.example.docgen.common.enums.OrganizationRole;
import com.example.docgen.enterprise.api.dto.OrganizationResponseDTO;
import com.example.docgen.enterprise.domain.Membership;
import com.example.docgen.enterprise.domain.Organization;
import com.example.docgen.enterprise.repositories.MembershipRepository;
import com.example.docgen.enterprise.repositories.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfilePfRepository userProfilePfRepository;
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;

    /**
     * Cria e salva um perfil de Pessoa Física, associando-o a um AuthUser existente.
     *
     * @param detachedUser A identidade do usuário autenticado.
     * @param dto          Os dados do perfil a serem criados.
     */
    @Transactional
    public void createPersonalProfile(AuthUser detachedUser, UserProfilePFRequest dto) {
        // Regra de negócio: Impede que um usuário crie mais de um perfil pessoal
        if (userProfilePfRepository.existsById(detachedUser.getId())) {
            throw new IllegalStateException("Este usuário já possui um perfil pessoal cadastrado.");
        }

        AuthUser managedUser = authUserRepository.getReferenceById(detachedUser.getId());

        UserProfilePF profile = UserProfilePF.builder()
                .user(managedUser)
                .fullName(dto.fullName())
                .cpf(dto.cpf()) // Assumindo que a validação de formato já ocorreu no DTO
                .birthDate(dto.birthDate() != null ? LocalDate.parse(dto.birthDate()) : null)
                .phone(dto.phone())
                .build();

        userProfilePfRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public ProfileMeResponseDTO getMyProfile(AuthUser user) {
        // Busca a versão gerenciada para carregar as coleções LAZY
        AuthUser managedUser = authUserRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // O novo DTO agora tem toda a lógica de mascaramento e construção
        return new ProfileMeResponseDTO(managedUser);
    }

    // --- NOVO MÉTODO PARA LISTAR AS ORGANIZAÇÕES DO USUÁRIO ---
    @Transactional(readOnly = true)
    public List<OrganizationResponseDTO> getMyOrganizations(AuthUser user) {
        // 1. Busca a versão "viva" do usuário para garantir que as afiliações sejam carregadas.
        AuthUser managedUser = authUserRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // 2. Mapeia a lista de 'memberships' do usuário para uma lista de DTOs de organização.
        return managedUser.getMemberships().stream()
                .map(membership -> OrganizationResponseDTO.fromEntity(membership.getOrganization()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void anonymizeAndDeactivateAccount(AuthUser user) {
        // 1. Busca a versão gerenciada e completa do usuário
        AuthUser managedUser = authUserRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // 2. Anonimiza o Perfil Pessoal (se existir)
        if (managedUser.getPersonalProfile() != null) {
            UserProfilePF profile = managedUser.getPersonalProfile();

            // Substitui dados sensíveis por valores anonimizados
            profile.setFullName("Usuário Removido");
            profile.setCpf("ANONIMIZADO_" + managedUser.getId().toString()); // Libera o CPF
            profile.setPhone(null);
            profile.setBirthDate(null);

            userProfilePfRepository.save(profile);
        }

        // 3. Anonimiza a Identidade
        // Altera o e-mail para um valor inválido e único para liberar o e-mail original
        managedUser.setEmail(managedUser.getId().toString() + "@inactive.user");

        // Altera a senha para um valor aleatório e inutilizável
        managedUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        // 4. Desativa a Conta (Soft Delete)
        managedUser.setEnabled(false);

        authUserRepository.save(managedUser);

        // Opcional: Desvincular de todas as organizações (embora um usuário PF não deva ter memberships)
        // membershipRepository.deleteAll(managedUser.getMemberships());
    }

    // --- NOVO MÉTODO DEDICADO PARA SAIR DA ORGANIZAÇÃO ---
    @Transactional
    public void leaveOrganization(AuthUser currentUser, UUID organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organização não encontrada."));

        // Encontra a afiliação específica do usuário com esta organização
        Membership membership = membershipRepository.findByOrganizationAndUser_Id(organization, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Você não é membro desta organização."));

        // Regra de negócio: só pode sair se não for o último admin
        if (membership.getRole() == OrganizationRole.ORG_ADMIN) {
            if (isLastAdmin(organization)) {
                throw new IllegalStateException("Não é possível sair pois você é o último administrador. Promova outro membro primeiro.");
            }
        }

        // Ação final: deleta a afiliação
        membershipRepository.delete(membership);
    }

    /**
     * Método auxiliar para verificar se uma organização tem apenas um administrador.
     */
    private boolean isLastAdmin(Organization organization) {
        return membershipRepository.countByOrganizationAndRole(organization, OrganizationRole.ORG_ADMIN) <= 1;
    }
}