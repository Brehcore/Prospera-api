package com.example.prospera.enterprise.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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