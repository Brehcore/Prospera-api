package com.example.docgen.enterprise.repositories;

import com.example.docgen.enterprise.domain.UserSector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserSectorRepository extends JpaRepository<UserSector, UUID> {
    List<UserSector> findByUserId(UUID userId);

    boolean existsByUserIdAndSectorId(UUID userId, UUID sectorId);

}
