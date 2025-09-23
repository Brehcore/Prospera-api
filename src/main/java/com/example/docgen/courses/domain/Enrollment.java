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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "course_enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // O usuário que está matriculado
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auth_user_id")
    private AuthUser user;

    // O curso no qual ele está matriculado
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_id")
    private Training training;

    // A organização que pagou pela matrícula (pode ser nulo para compras individuais de PF)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization sponsoredBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    // Percentual de progresso, ex: 80.5
    @Column(precision = 5, scale = 2)
    private BigDecimal progressPercentage;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime enrolledAt;

    private OffsetDateTime completedAt;

    @PrePersist
    void prePersist() {
        enrolledAt = OffsetDateTime.now();
    }
}