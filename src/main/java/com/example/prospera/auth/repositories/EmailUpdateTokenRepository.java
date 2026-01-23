package com.example.prospera.auth.repositories;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.auth.domain.EmailUpdateToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailUpdateTokenRepository extends JpaRepository<EmailUpdateToken, UUID> {
    Optional<EmailUpdateToken> findByVerificationCodeAndUser(String code, AuthUser user);

    // Remove tokens antigos do mesmo usuário para não acumular lixo
    void deleteByUser(AuthUser user);
}