package com.example.docgen.enterprise.service;

import com.example.docgen.courses.repositories.TrainingSectorAssignmentRepository;
import com.example.docgen.enterprise.api.dto.SectorDTO;
import com.example.docgen.enterprise.domain.Sector;
import com.example.docgen.enterprise.repositories.OrganizationSectorRepository;
import com.example.docgen.enterprise.repositories.SectorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectorService {

    private final SectorRepository sectorRepository;
    private final TrainingSectorAssignmentRepository assignmentRepository;
    private final OrganizationSectorRepository organizationSectorRepository;

    /**
     * Cria um novo setor global
     */
    @Transactional
    public Sector createSector(SectorDTO dto) {
        // Opcional: Adicionar validação para não criar setores com nomes duplicados
        Sector newSector = new Sector();
        newSector.setName(dto.name());
        return sectorRepository.save(newSector);
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

    @Transactional(readOnly = true)
    public List<SectorDTO> findAllPublicSectors() {
        return sectorRepository.findAll().stream()
                .map(SectorDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // mover outras lógicas de gestão de setores para cá em breve,
    // como criar ou editar um setor.
}