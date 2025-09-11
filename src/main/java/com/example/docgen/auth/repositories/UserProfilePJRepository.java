package com.example.docgen.auth.repositories;

import com.example.docgen.auth.domain.UserProfilePJ;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface UserProfilePJRepository extends JpaRepository<UserProfilePJ, UUID> {
    boolean existsByCnpj(String cnpj);
}