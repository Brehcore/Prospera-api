package com.example.docgen.enterprise.repositories;

import com.example.docgen.enterprise.domain.Membership;
import com.example.docgen.enterprise.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    // O método findByOrganization foi substituído pelo método otimizado abaixo

    /**
     * Busca todas as afiliações de uma organização, já carregando (FETCH) os dados
     * do usuário (user) e do perfil pessoal (personalProfile) de cada membro em uma
     * única consulta otimizada para evitar o problema N+1 e LazyInitializationException.
     */
    @Query("SELECT m FROM Membership m " +
            "JOIN FETCH m.user u " +
            "LEFT JOIN FETCH u.personalProfile " +
            "WHERE m.organization = :organization")
    List<Membership> findByOrganizationWithDetails(@Param("organization") Organization organization);

    Optional<Membership> findByOrganizationAndUser_Id(Organization organization, UUID userId);
}