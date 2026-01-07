package com.example.prospera.subscription.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Classe Objeto auxiliar que serve como o identificador completo para a entidade PlanTraining
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PlanTrainingId implements Serializable {

    @Column(name = "plan_id")
    private UUID planId;

    @Column(name = "training_id")
    private UUID trainingId;
}
