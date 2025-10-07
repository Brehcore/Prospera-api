package com.example.docgen.subscription.repositories;

import com.example.docgen.subscription.entities.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanRepository extends JpaRepository<Plan, UUID> {

    List<Plan> findAllByIsActive(boolean isActive);

    boolean existsByName(String name);

}
