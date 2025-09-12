package com.example.docgen.auth.repositories;

import com.example.docgen.auth.domain.UserProfilePF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserProfilePfRepository extends JpaRepository<UserProfilePF, UUID> {

    // Método para verificar se um CPF já existe no banco de dados
    boolean existsByCpf(String cpf);
}