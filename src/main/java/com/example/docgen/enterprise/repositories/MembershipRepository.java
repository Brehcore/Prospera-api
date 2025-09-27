package com.example.docgen.enterprise.repositories;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.common.enums.OrganizationRole;
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

    // Método para a verificação do último admin
    long countByOrganizationAndRole(Organization organization, OrganizationRole role);

    // Método para a verificação de membro duplicado
    boolean existsByUserAndOrganization(AuthUser user, Organization organization);

    Optional<Membership> findByUserId(UUID userId);

    /**
     * Busca uma afiliação pelo ID e já carrega (JOIN FETCH) os dados do usuário,
     * o perfil pessoal do usuário e o admin que o adicionou, tudo em uma única consulta.
     */
    @Query("SELECT m FROM Membership m " +
            "LEFT JOIN FETCH m.user u " +
            "LEFT JOIN FETCH u.personalProfile " +
            "LEFT JOIN FETCH m.addedBy " +
            "WHERE m.id = :membershipId")
    Optional<Membership> findWithDetailsById(@Param("membershipId") UUID membershipId);

}