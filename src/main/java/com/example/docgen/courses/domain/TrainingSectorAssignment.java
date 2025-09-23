package com.example.docgen.courses.domain;

import com.example.docgen.courses.domain.enums.TrainingType;
import jakarta.persistence.*;
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