package com.example.prospera.auth.repositories;

import com.example.prospera.auth.domain.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {

    Optional<AuthUser> findByEmail(String email);

    @Query("SELECT u FROM AuthUser u LEFT JOIN FETCH u.personalProfile LEFT JOIN FETCH u.memberships WHERE u.id = :id")
    Optional<AuthUser> findUserWithDetailsById(@Param("id") UUID id);

    boolean existsByEmail(String email);

    Page<AuthUser> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}