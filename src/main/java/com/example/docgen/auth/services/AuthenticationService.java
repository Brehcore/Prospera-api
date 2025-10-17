package com.example.docgen.auth.services;

import com.example.docgen.auth.api.dto.UserRegisterRequest;
import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.repositories.AuthUserRepository;
import com.example.docgen.common.enums.OrganizationRole;
import com.example.docgen.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return authUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    @Transactional
    public AuthUser registerIdentity(UserRegisterRequest dto) {
        if (authUserRepository.findByEmail(dto.email()).isPresent()) {
            throw new IllegalStateException("Email já está em uso.");
        }
        String hashedPassword = passwordEncoder.encode(dto.password());
        var newUser = new AuthUser(dto.email(), hashedPassword, UserRole.USER);
        return authUserRepository.save(newUser);
    }

    @Transactional
    public void adminResetPassword(String email, String newPassword) {
        AuthUser user = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para resetar a senha."));

        user.setPassword(passwordEncoder.encode(newPassword));
        authUserRepository.save(user);
    }

    /**
     * Verifica se um usuário é ORG_ADMIN de QUALQUER organização
     * dentro de uma Account específica.
     * Lança AccessDeniedException se a verificação falhar.
     *
     * @param user      O usuário cujas permissões serão verificadas.
     * @param accountId O ID da Account que se deseja gerenciar.
     */
    @Transactional(readOnly = true)
    public void checkIsAdminOfAccount(AuthUser user, UUID accountId) {
        if (user.getMemberships() == null || user.getMemberships().isEmpty()) {
            throw new AccessDeniedException("Acesso negado. O usuário не é membro de nenhuma organização.");
        }

        boolean hasAdminRightsForAccount = user.getMemberships().stream()
                .anyMatch(membership ->
                        // A organização da filiação pertence à conta que queremos gerenciar?
                        membership.getOrganization().getAccount().getId().equals(accountId) &&
                                // E o papel do usuário é de administrador?
                                membership.getRole() == OrganizationRole.ORG_ADMIN
                );

        if (!hasAdminRightsForAccount) {
            throw new AccessDeniedException("Acesso negado. O usuário não tem permissão de administrador para esta conta.");
        }
    }
}