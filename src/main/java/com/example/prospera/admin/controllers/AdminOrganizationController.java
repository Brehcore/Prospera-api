package com.example.prospera.admin.controllers;

import com.example.prospera.admin.dto.AdminOrganizationDetailDTO;
import com.example.prospera.admin.dto.AdminOrganizationSummaryDTO;
import com.example.prospera.admin.dto.UpdateOrgStatusRequest;
import com.example.prospera.admin.services.AdminOrganizationService;
import com.example.prospera.enterprise.dto.SectorDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller responsável por gerenciar organizações no contexto administrativo.
 *
 * <p>Esta classe define endpoints para que o administrador do sistema possa visualizar
 * e gerenciar organizações. Apenas usuários com a função 'SYSTEM_ADMIN' têm permissão
 * para acessar os métodos desta classe.</p>
 */
@RestController
@RequestMapping("/admin/organizations")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class AdminOrganizationController {

    private final AdminOrganizationService adminOrgService;

    /**
     * Lista todas as organizações cadastradas no sistema.
     *
     * @return Lista contendo os resumos de todas as organizações disponíveis.
     */
    @GetMapping
    public ResponseEntity<List<AdminOrganizationSummaryDTO>> getAllOrganizations() {
        List<AdminOrganizationSummaryDTO> orgs = adminOrgService.getAllOrganizations().stream()
                .map(AdminOrganizationSummaryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orgs);
    }

    /**
     * Obtém os detalhes de uma organização específica.
     *
     * @param organizationId ID único da organização.
     * @return Detalhes completos da organização correspondente ao ID.
     */
    @GetMapping("/{organizationId}")
    public ResponseEntity<AdminOrganizationDetailDTO> getOrganizationDetails(@PathVariable UUID organizationId) {
        AdminOrganizationDetailDTO orgDetails = AdminOrganizationDetailDTO.fromEntity(
                adminOrgService.getOrganizationDetails(organizationId)
        );
        return ResponseEntity.ok(orgDetails);
    }

    /**
     * Atualiza o status de uma organização.
     *
     * @param organizationId ID único da organização cujo status será alterado.
     * @param request        Objeto contendo o novo status a ser aplicado.
     * @return Resposta sem conteúdo com código HTTP 204 se a atualização for bem-sucedida.
     */
    @PatchMapping("/{organizationId}/status")
    public ResponseEntity<Void> updateOrganizationStatus(
            @PathVariable UUID organizationId,
            @RequestBody @Valid UpdateOrgStatusRequest request) {

        adminOrgService.updateOrganizationStatus(organizationId, request.newStatus());
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtém a lista de setores associados a uma organização específica.
     *
     * @param organizationId ID único da organização.
     * @return Lista de setores associados à organização fornecida.
     */
    @GetMapping("/{organizationId}/sectors")
    public ResponseEntity<List<SectorDTO>> getOrganizationSectors(@PathVariable UUID organizationId) {
        List<SectorDTO> sectors = adminOrgService.getSectorsForOrganization(organizationId);
        return ResponseEntity.ok(sectors);
    }
}