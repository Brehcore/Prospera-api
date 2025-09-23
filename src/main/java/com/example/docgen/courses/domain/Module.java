package com.example.docgen.courses.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
// CORREÇÃO 1: Nome da tabela padronizado para "training"
@Table(name = "training_modules")
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // CORREÇÃO 2: A relação é com RecordedCourse, não Training.
    // O nome do campo "course" é mantido para ser compatível com o 'mappedBy="course"' em RecordedCourse.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // CORREÇÃO 3: Nome da coluna padronizado para "training"
    @JoinColumn(name = "training_id")
    private RecordedCourse course;

    @Column(nullable = false)
    private String title;

    @Column(name = "module_order", nullable = false) // Renomeado para clareza no DB
    private int moduleOrder;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lessonOrder ASC")
    private List<Lesson> lessons;

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