package com.example.docgen.courses.domain;

import com.example.docgen.courses.domain.enums.PublicationStatus;
import com.example.docgen.courses.domain.enums.TrainingEntityType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
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