package com.example.prospera.enterprise.service;

import com.example.prospera.enterprise.dto.SectorDTO;
import com.example.prospera.enterprise.repositories.SectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectorService {

    private final SectorRepository sectorRepository;

    @Transactional(readOnly = true)
    public List<SectorDTO> findAllPublicSectors() {
        return sectorRepository.findAll().stream()
                .map(SectorDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SectorDTO> findSectorsByIds(Set<UUID> sectorIds) {
        if (sectorIds == null || sectorIds.isEmpty()) {
            return Collections.emptyList();
        }
        // Supondo que você tenha um SectorRepository injetado
        return sectorRepository.findAllById(sectorIds).stream()
                .map(SectorDTO::fromEntity)
                .collect(Collectors.toList());
    }

}