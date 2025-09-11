package com.example.docgen.auth.services;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.dto.UserRequestDTO;
import com.example.docgen.auth.repositories.AuthUserRepository;
import com.example.docgen.common.enums.UserRole;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    // REMOVIDO: AuthenticationManager e JwtService
    public AuthenticationService(AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return authUserRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + username));
    }

    public AuthUser register(UserRequestDTO dto) {
        if (authUserRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalStateException("O email informado já está cadastrado.");
        }
        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        AuthUser newUser = new AuthUser(dto.getEmail(), hashedPassword, UserRole.USER);
        return authUserRepository.save(newUser);
    }

}