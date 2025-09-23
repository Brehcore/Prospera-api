package com.example.docgen.enterprise.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Essa classe atribui um setor a um usuário. Serve para determinar quais treinamentos um usuário dever ver.
 */

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_sectors", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "sector_id"})
})
public class UserSector {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "sector_id", nullable = false)
    private UUID sectorId;

    // Nulo para usuários PF, preenchido para membros de uma organização
    @Column(name = "organization_id")
    private UUID organizationId;

    public UserSector(UUID userId, UUID sectorId, UUID organizationId) {
        this.userId = userId;
        this.sectorId = sectorId;
        this.organizationId = organizationId;
    }
}