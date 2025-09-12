package com.example.docgen.auth.services;

import com.example.docgen.auth.api.dto.ProfileMeResponseDTO;
import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.domain.UserProfilePF;
import com.example.docgen.auth.api.dto.UserProfilePFRequest;
import com.example.docgen.auth.repositories.AuthUserRepository;
import com.example.docgen.auth.repositories.UserProfilePfRepository;
import com.example.docgen.enterprise.api.dto.OrganizationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfilePfRepository userProfilePfRepository;
    private final AuthUserRepository authUserRepository;

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
}