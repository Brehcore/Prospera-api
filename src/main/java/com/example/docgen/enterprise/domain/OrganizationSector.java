package com.example.docgen.enterprise.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "organization_sectors", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"organization_id", "sector_id"}) // Garante que a combinação é única
})
public class OrganizationSector {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "sector_id", nullable = false)
    private UUID sectorId; // Apenas a referência ao ID do Setor global

    public OrganizationSector(Organization organization, UUID sectorId) {
        this.organization = organization;
        this.sectorId = sectorId;
    }
}