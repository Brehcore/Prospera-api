package com.example.docgen.enterprise.repositories;

import com.example.docgen.enterprise.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    boolean existsByCnpj(String cnpj);
}