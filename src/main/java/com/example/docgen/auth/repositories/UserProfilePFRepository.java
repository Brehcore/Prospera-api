package com.example.docgen.auth.repositories;

import com.example.docgen.auth.domain.UserProfilePF;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface UserProfilePFRepository extends JpaRepository<UserProfilePF, UUID> {
    boolean existsByCpf(String cpf);
}