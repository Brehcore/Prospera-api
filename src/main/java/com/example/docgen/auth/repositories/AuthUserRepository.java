package com.example.docgen.auth.repositories;

import com.example.docgen.auth.domain.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {

    Optional<AuthUser> findByEmail(String email);

    @Query("SELECT u FROM AuthUser u LEFT JOIN FETCH u.personalProfile LEFT JOIN FETCH u.memberships WHERE u.id = :id")
    Optional<AuthUser> findUserWithDetailsById(@Param("id") UUID id);

    List<AuthUser> findByEmailContainingIgnoreCase(String email);

}