package com.example.prospera.courses.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "training_ratings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Vincula a avaliação a uma matrícula específica.
    // Usamos OneToOne para garantir que o aluno avalie apenas 1 vez por matrícula.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    private Enrollment enrollment;

    @Column(nullable = false)
    private Integer score; // 1 a 5

    @Column(length = 500)
    private String comment; // Opcional: "Gostei muito, mas..."

    @Column(nullable = false)
    private OffsetDateTime ratedAt;

    @PrePersist
    void prePersist() {
        ratedAt = OffsetDateTime.now();
    }
}