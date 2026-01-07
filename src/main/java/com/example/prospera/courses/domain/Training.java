package com.example.prospera.courses.domain;

import com.example.prospera.courses.domain.enums.PublicationStatus;
import com.example.prospera.courses.domain.enums.TrainingEntityType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "trainings")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // Define que é uma classe pai
@DiscriminatorColumn(name = "entity_type", discriminatorType = DiscriminatorType.STRING) // Coluna que define o tipo
public abstract class Training { // <-- CORREÇÃO: A classe agora é abstrata

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // --- CAMPOS COMUNS A TODOS OS TREINAMENTOS (O QUE FOI MANTIDO) ---

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String author;

    // Este é o campo que diferencia EBOOK, RECORDED_COURSE, etc.
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", insertable = false, updatable = false)
    private TrainingEntityType entityType;

    // Status global do treinamento (rascunho, publicado, etc.)
    @Enumerated(EnumType.STRING)
    private PublicationStatus status;

    // Para treinamentos que são exclusivos de uma organização
    @Column(name = "organization_id")
    private UUID organizationId;

    // --- CAMPOS DE AUDITORIA ---

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    private String coverImageUrl;

    @OneToMany(mappedBy = "trainingId", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingSectorAssignment> sectorAssignments;

    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}