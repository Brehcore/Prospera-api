package com.example.docgen.auth.services;

import com.example.docgen.auth.api.dto.UserRegisterRequest;
import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.repositories.AuthUserRepository;
import com.example.docgen.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}