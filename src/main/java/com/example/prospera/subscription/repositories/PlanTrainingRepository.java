package com.example.prospera.subscription.repositories;

import com.example.prospera.subscription.entities.PlanTraining;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlanTrainingRepository extends JpaRepository<PlanTraining, UUID> {
}
