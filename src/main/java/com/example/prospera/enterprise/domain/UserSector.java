package com.example.prospera.enterprise.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Classe que representa a associação entre um usuário e um setor específico.
 * Esta entidade é fundamental para o sistema de permissionamento e distribuição de conteúdo,
 * pois determina quais treinamentos um usuário deve ter acesso baseado em seus setores atribuídos.
 *
 * Um usuário pode estar associado a múltiplos setores, e estes podem estar vinculados
 * a uma organização (para usuários corporativos) ou não (para usuários pessoa física).
 *
 * @see com.example.prospera.enterprise.domain.Sector
 * @see com.example.prospera.enterprise.service.SectorAssignmentService
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "user_sectors", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "sector_id"})
})
public class UserSector {

    /**
     * Identificador único da associação usuário-setor
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * ID do usuário associado ao setor
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * ID do setor ao qual o usuário está associado
     */
    @Column(name = "sector_id", nullable = false)
    private UUID sectorId;

    /**
     * ID da organização à qual esta associação pertence.
     * Este campo será nulo para usuários pessoa física (PF) e
     * preenchido para usuários que são membros de uma organização.
     */
    @Column(name = "organization_id")
    private UUID organizationId;

    /**
     * Construtor para criar uma nova associação usuário-setor
     *
     * @param userId         ID do usuário a ser associado
     * @param sectorId       ID do setor a ser associado
     * @param organizationId ID da organização (pode ser nulo para usuários PF)
     */
    public UserSector(UUID userId, UUID sectorId, UUID organizationId) {
        this.userId = userId;
        this.sectorId = sectorId;
        this.organizationId = organizationId;
    }
    
    
}