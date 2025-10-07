package com.example.docgen.subscription.entities;

import com.example.docgen.courses.domain.Training;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "plan_trainings")
public class PlanTraining {

    @EmbeddedId // Usa a classe PlanTrainingId como a chave primária
    private PlanTrainingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("planId") // Mapeia a parte 'planId' da chave composta para esta relação
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("trainingId") // Mapeia a parte 'trainingId' da chave composta para esta relação
    @JoinColumn(name = "training_id")
    private Training training;

}
