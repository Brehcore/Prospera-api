package com.example.docgen.enterprise.repositories;

import com.example.docgen.enterprise.domain.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SectorRepository extends JpaRepository<Sector, UUID> {
}