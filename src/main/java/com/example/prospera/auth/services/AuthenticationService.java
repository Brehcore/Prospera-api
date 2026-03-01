package com.example.prospera.auth.services;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.auth.domain.EmailUpdateToken;
import com.example.prospera.auth.domain.PasswordResetToken;
import com.example.prospera.auth.dto.UserRegisterRequest;
import com.example.prospera.auth.repositories.AuthUserRepository;
import com.example.prospera.auth.repositories.EmailUpdateTokenRepository;
import com.example.prospera.auth.repositories.PasswordResetTokenRepository;
import com.example.prospera.common.enums.OrganizationRole;
import com.example.prospera.common.enums.UserRole;
import com.example.prospera.exceptions.ResourceNotFoundException;
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
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final EmailUpdateTokenRepository emailUpdateTokenRepository;

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

    @Transactional
    public void forgotPassword(String email) {
        AuthUser user = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        // Remove token antigo se houver
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

        // Gera novo token
        String token = UUID.randomUUID().toString();
        PasswordResetToken myToken = new PasswordResetToken(token, user);
        tokenRepository.save(myToken);

        // Envia e-mail
        emailService.sendResetTokenEmail(user.getEmail(), token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido."));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken); // Limpa token expirado
            throw new IllegalArgumentException("Token expirado. Solicite uma nova redefinição.");
        }

        AuthUser user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        authUserRepository.save(user);

        tokenRepository.delete(resetToken); // Token usado é deletado
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // 1. Valida se a senha atual bate com a do banco
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("A senha atual está incorreta.");
        }

        // 2. Atualiza para a nova senha
        user.setPassword(passwordEncoder.encode(newPassword));
        authUserRepository.save(user);
    }

    @Transactional
    public void initiateEmailChange(UUID userId, String currentEmailInput, String newEmail) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // 1. Valida se o input do e-mail atual está certo
        if (!user.getEmail().equalsIgnoreCase(currentEmailInput)) {
            throw new IllegalArgumentException("O e-mail atual informado está incorreto.");
        }

        // 2. Valida se o novo e-mail já existe no banco
        if (authUserRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Este novo e-mail já está em uso.");
        }

        // 3. Limpa tokens antigos pendentes deste usuário
        emailUpdateTokenRepository.deleteByUser(user);

        // 4. Gera código de 6 dígitos
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);

        // 5. Salva a intenção de troca
        EmailUpdateToken token = new EmailUpdateToken(code, newEmail, user);
        emailUpdateTokenRepository.save(token);

        // 6. Envia para o E-MAIL ATUAL (ANTIGO)
        emailService.sendEmailChangeCode(user.getEmail(), code);
    }

    @Transactional
    public void confirmEmailChange(UUID userId, String code) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // 1. Busca o token
        EmailUpdateToken token = emailUpdateTokenRepository.findByVerificationCodeAndUser(code, user)
                .orElseThrow(() -> new IllegalArgumentException("Código inválido ou não encontrado."));

        // 2. Verifica expiração
        if (token.isExpired()) {
            emailUpdateTokenRepository.delete(token);
            throw new IllegalArgumentException("O código expirou. Solicite novamente.");
        }

        // 3. Verifica novamente se o e-mail alvo ainda está livre (pode ter sido pego nesses 15 min)
        if (authUserRepository.existsByEmail(token.getNewPendingEmail())) {
            throw new IllegalArgumentException("O e-mail solicitado acabou de ser ocupado por outro usuário.");
        }

        // 4. Efetiva a troca
        user.setEmail(token.getNewPendingEmail());
        authUserRepository.save(user);

        // 5. Limpa o token usado
        emailUpdateTokenRepository.delete(token);
    }
}