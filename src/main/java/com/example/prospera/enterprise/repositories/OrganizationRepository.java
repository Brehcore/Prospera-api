package com.example.prospera.enterprise.repositories;

import com.example.prospera.enterprise.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    boolean existsByCnpj(String cnpj);

    @Query("SELECT o FROM Organization o LEFT JOIN FETCH o.memberships m LEFT JOIN FETCH m.user WHERE o.id = :id")
    Optional<Organization> findWithMembersById(@Param("id") UUID id);
}