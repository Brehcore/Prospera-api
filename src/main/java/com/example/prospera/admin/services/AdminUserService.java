package com.example.prospera.admin.services;

import com.example.prospera.admin.dto.AdminUserUpdateRequest;
import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.auth.repositories.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pelas operações relacionadas aos usuários administrativos no sistema.
 * Oferece funcionalidades como redefinir senha, buscar usuários, detalhar usuários,
 * e ativar/desativar suas contas. Todas as operações manipulam objetos {@link AuthUser}.
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Redefine a senha de um usuário administrativo identificado pelo email fornecido.
     * A nova senha fornecida será codificada antes de ser armazenada.
     *
     * @param email       Email do usuário cuja senha será redefinida.
     * @param newPassword Nova senha que será atribuída ao usuário.
     * @throws RuntimeException se o usuário não for encontrado.
     */
    @Transactional
    public void adminResetPassword(String email, String newPassword) {
        AuthUser user = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        user.setPassword(passwordEncoder.encode(newPassword));
        authUserRepository.save(user);
    }

    /**
     * Retorna uma lista de usuários administrativos. Caso um email seja fornecido,
     * filtra os usuários pelo cadastro correspondente; caso contrário, retorna todos os usuários.
     *
     * @param email (opcional) Filtro pelo email dos usuários.
     * @return Lista de usuários administrativos encontrados.
     */
    @Transactional(readOnly = true)
    public Page<AuthUser> getAllUsers(String email, Pageable pageable) {
        if (email != null && !email.isBlank()) {
            return authUserRepository.findByEmailContainingIgnoreCase(email, pageable);
        } else {
            return authUserRepository.findAll(pageable);
        }
    }

    /**
     * Recupera os detalhes completos de um usuário administrativo específico pelo seu ID.
     * Usa uma consulta com JOIN FETCH para evitar problemas de lazy loading.
     *
     * @param userId Identificador único do usuário.
     * @return Objeto {@link AuthUser} contendo os detalhes do usuário.
     * @throws RuntimeException se o usuário não for encontrado.
     */
    @Transactional(readOnly = true)
    public AuthUser getUserDetails(UUID userId) {
        // Usamos uma query com JOIN FETCH para evitar problemas de lazy loading
        return authUserRepository.findUserWithDetailsById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + userId));
    }

    /**
     * Desativa a conta de um usuário administrativo, impedindo seu acesso ao sistema.
     *
     * @param userId Identificador único do usuário.
     * @throws RuntimeException se o usuário não for encontrado.
     */
    @Transactional
    public void deactivateUser(UUID userId) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + userId));
        user.setEnabled(false);
        authUserRepository.save(user);
    }

    /**
     * Ativa a conta de um usuário administrativo previamente desativado,
     * permitindo que o usuário recupere acesso ao sistema.
     *
     * @param userId Identificador único do usuário.
     * @throws RuntimeException se o usuário não for encontrado.
     */
    @Transactional
    public void activateUser(UUID userId) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + userId));
        user.setEnabled(true);
        authUserRepository.save(user);
    }

    /**
     * Atualiza os dados de um usuário e de seu perfil pessoal.
     */
    @Transactional
    public AuthUser updateUser(UUID userId, AdminUserUpdateRequest request) {
        // Usamos o método com JOIN FETCH para já trazer o perfil pessoal junto
        AuthUser user = authUserRepository.findUserWithDetailsById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + userId));

        // 1. Atualiza dados da entidade AuthUser
        if (request.role() != null) {
            user.setRole(request.role());
        }

        // 2. Atualiza dados do Perfil Pessoal (se existir)
        if (user.getPersonalProfile() != null) {
            if (request.fullName() != null) user.getPersonalProfile().setFullName(request.fullName());
            if (request.cpf() != null) user.getPersonalProfile().setCpf(request.cpf());
            if (request.birthDate() != null) user.getPersonalProfile().setBirthDate(request.birthDate());
            if (request.phone() != null) user.getPersonalProfile().setPhone(request.phone());
        }

        return authUserRepository.save(user);
    }
}