package com.example.prospera.courses.domain;

import com.example.prospera.courses.domain.enums.TrainingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "training_sector_assignments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"training_id", "sector_id"})
})
public class TrainingSectorAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "training_id", nullable = false)
    private UUID trainingId;

    @Column(name = "sector_id", nullable = false)
    private UUID sectorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingType trainingType; // COMPULSORY ou ELECTIVE (CORRETO, varia por setor)

    private String legalBasis; // CORRETO, pode variar por setor
}