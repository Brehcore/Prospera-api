package com.example.docgen.enterprise.repositories;

import com.example.docgen.enterprise.domain.Sector;
import com.example.docgen.enterprise.domain.UserSector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserSectorRepository extends JpaRepository<UserSector, UUID> {
    List<UserSector> findByUserId(UUID userId);

    boolean existsByUserIdAndSectorId(UUID userId, UUID sectorId);

    /**
     * Encontra todas as entidades Sector associadas a um userId específico
     * através da tabela de ligação UserSector.
     */
    @Query("SELECT s FROM Sector s JOIN UserSector us ON s.id = us.sectorId WHERE us.userId = :userId")
    List<Sector> findSectorsByUserId(@Param("userId") UUID userId);

}
