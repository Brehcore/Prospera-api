package com.example.docgen.courses.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("LIVE_TRAINING") // Identifica esta classe na tabela 'trainings'
public class LiveTraining extends Training {

    private String meetingUrl; // Link para a sala (Zoom, Meet, etc.)

    private OffsetDateTime startDateTime; // Data e hora de início do treinamento

}