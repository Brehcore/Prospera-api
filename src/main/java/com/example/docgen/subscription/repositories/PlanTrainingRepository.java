package com.example.docgen.subscription.repositories;

import com.example.docgen.subscription.entities.PlanTraining;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlanTrainingRepository extends JpaRepository<PlanTraining, UUID> {
}
