package com.example.prospera.admin.controllers;

import com.example.prospera.admin.services.AdminSectorService;
import com.example.prospera.enterprise.domain.Sector;
import com.example.prospera.enterprise.dto.SectorDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/sectors")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class AdminSectorController {

    private final AdminSectorService adminSectorService;

    @PostMapping
    public ResponseEntity<SectorDTO> createSector(@RequestBody SectorDTO dto) {
        Sector savedSector = adminSectorService.createSector(dto);
        return ResponseEntity.ok(new SectorDTO(savedSector.getId(), savedSector.getName()));
    }

    @GetMapping
    public ResponseEntity<List<SectorDTO>> getAllSectors() {
        return ResponseEntity.ok(adminSectorService.getAllSectors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectorDTO> getSectorById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminSectorService.getSectorById(id));
    }

    /**
     * Exclui um setor global do sistema.
     */
    @DeleteMapping("/{sectorId}")
    public ResponseEntity<Void> deleteSector(@PathVariable UUID sectorId) {
        adminSectorService.deleteSector(sectorId);
        return ResponseEntity.noContent().build();
    }
}