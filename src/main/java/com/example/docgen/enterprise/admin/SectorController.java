package com.example.docgen.enterprise.admin;

import com.example.docgen.enterprise.admin.dto.SectorDTO;
import com.example.docgen.enterprise.domain.Sector;
import com.example.docgen.enterprise.repositories.SectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/sectors")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class SectorController {

    private final SectorRepository sectorRepository; // Para um CRUD simples, podemos injetar o repositório diretamente

    @PostMapping
    public ResponseEntity<SectorDTO> createSector(@RequestBody SectorDTO dto) {
        Sector newSector = new Sector(null, dto.name());
        Sector savedSector = sectorRepository.save(newSector);
        return ResponseEntity.ok(new SectorDTO(savedSector.getId(), savedSector.getName()));
    }

    @GetMapping
    public ResponseEntity<List<SectorDTO>> getAllSectors() {
        List<SectorDTO> sectors = sectorRepository.findAll().stream()
                .map(s -> new SectorDTO(s.getId(), s.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(sectors);
    }

    // Futuramente, pode adicionar endpoints PUT e DELETE aqui
}