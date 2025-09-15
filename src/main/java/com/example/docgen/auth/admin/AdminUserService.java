package com.example.docgen.auth.admin;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.repositories.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void adminResetPassword(String email, String newPassword) {
        AuthUser user = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        user.setPassword(passwordEncoder.encode(newPassword));
        authUserRepository.save(user);
    }

    // --- NOVO MÉTODO PARA LISTAR TODOS OS USUÁRIOS ---
    @Transactional(readOnly = true)
    public List<AuthUser> getAllUsers(String email) {
        if (email != null && !email.isBlank()) {
            // Se um email foi passado, busca por ele
            return authUserRepository.findByEmailContainingIgnoreCase(email);
        } else {
            // Se nenhum email foi passado, retorna todos
            return authUserRepository.findAll(Sort.by("email"));
        }
    }

    // --- NOVO MÉTODO PARA BUSCAR DETALHES DE UM USUÁRIO ---
    @Transactional(readOnly = true)
    public AuthUser getUserDetails(UUID userId) {
        // Usamos uma query com JOIN FETCH para evitar problemas de lazy loading
        return authUserRepository.findUserWithDetailsById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + userId));
    }
}