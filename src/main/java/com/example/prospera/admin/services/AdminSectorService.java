package com.example.prospera.admin.services;

import com.example.prospera.courses.repositories.TrainingSectorAssignmentRepository;
import com.example.prospera.enterprise.domain.Sector;
import com.example.prospera.enterprise.dto.SectorDTO;
import com.example.prospera.enterprise.repositories.OrganizationSectorRepository;
import com.example.prospera.enterprise.repositories.SectorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminSectorService {

    private final SectorRepository sectorRepository;
    private final OrganizationSectorRepository organizationSectorRepository;
    private final TrainingSectorAssignmentRepository assignmentRepository;

    /**
     * Cria um novo setor global
     */
    @Transactional
    public Sector createSector(SectorDTO dto) {
        Sector newSector = new Sector();
        newSector.setName(dto.name());
        return sectorRepository.save(newSector);
    }

    /**
     * Retorna todos os setores cadastrados no sistema.
     */
    @Transactional(readOnly = true)
    public List<SectorDTO> getAllSectors() {
        return sectorRepository.findAll().stream()
                .map(SectorDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Busca um setor específico pelo seu ID.
     */
    @Transactional(readOnly = true)
    public SectorDTO getSectorById(UUID id) {
        Sector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado com o ID: " + id));
        return SectorDTO.fromEntity(sector);
    }


    /**
     * Exclui um setor global do sistema, mas apenas se ele não estiver em uso.
     * Esta é a lógica que seu AdminSectorController irá chamar.
     */
    @Transactional
    public void deleteSector(UUID sectorId) {
        // Validação 1: O setor existe?
        if (!sectorRepository.existsById(sectorId)) {
            throw new EntityNotFoundException("Setor não encontrado com o ID: " + sectorId);
        }

        // Validação 2 (SEGURANÇA): Verifica se algum treinamento está vinculado a este setor.
        if (assignmentRepository.existsBySectorId(sectorId)) {
            throw new IllegalStateException("Não é possível excluir este setor, pois ele está associado a treinamentos.");
        }

        // Validação 3 (SEGURANÇA): Verifica se alguma organização "adotou" este setor.
        if (organizationSectorRepository.existsBySectorId(sectorId)) {
            throw new IllegalStateException("Não é possível excluir este setor, pois ele está em uso por uma ou mais organizações.");
        }

        // Se todas as validações passaram, a exclusão é segura.
        sectorRepository.deleteById(sectorId);
    }
}
