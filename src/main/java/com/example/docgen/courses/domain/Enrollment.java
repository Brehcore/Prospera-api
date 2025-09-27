package com.example.docgen.courses.domain;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.courses.domain.enums.EnrollmentStatus;
import com.example.docgen.enterprise.domain.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representa a matrícula de um usuário em um curso/treinamento.
 * Esta entidade mantém o registro e o progresso do aluno em um curso específico,
 * incluindo informações sobre quem patrocinou a matrícula (se aplicável) e o status atual.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "course_enrollments")
public class Enrollment {

    /**
     * Identificador único da matrícula
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Usuário matriculado no curso
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auth_user_id")
    private AuthUser user;

    /**
     * Curso/treinamento em que o usuário está matriculado
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_id")
    private Training training;

    /**
     * Organização que patrocinou/pagou pela matrícula.
     * Pode ser nulo em caso de compras individuais por pessoa física.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization sponsoredBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime enrolledAt;

    private OffsetDateTime completedAt;

    @PrePersist
    void prePersist() {
        enrolledAt = OffsetDateTime.now();
    }
}