package com.example.prospera.enterprise.api.controllers;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.courses.api.dto.TrainingSummaryDTO;
import com.example.prospera.courses.service.AdminTrainingService;
import com.example.prospera.courses.service.EnrollmentService;
import com.example.prospera.enterprise.api.dto.MassEnrollmentRequest;
import com.example.prospera.enterprise.api.dto.MemberResponseDTO;
import com.example.prospera.enterprise.api.dto.SectorDTO;
import com.example.prospera.enterprise.api.dto.SectorIdRequest;
import com.example.prospera.enterprise.domain.Organization;
import com.example.prospera.enterprise.repositories.OrganizationRepository;
import com.example.prospera.enterprise.service.SectorAssignmentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controlador responsável por gerenciar operações administrativas de uma organização.
 * Permite que administradores da organização (ORG_ADMIN) realizem operações como:
 * - Matricular membros em treinamentos
 * - Gerenciar setores da organização
 * - Consultar treinamentos disponíveis
 * - Visualizar membros matriculados
 */
@RestController
@RequestMapping("/organizations/{orgId}")
@PreAuthorize("hasRole('ORG_ADMIN')")
@RequiredArgsConstructor
public class OrganizationAdminController {

    private final EnrollmentService enrollmentService;
    private final SectorAssignmentService sectorAssignmentService;
    private final OrganizationRepository organizationRepository;
    private final AdminTrainingService adminTrainingService;

    /**
     * Realiza a matrícula em massa de vários membros da organização em um treinamento específico.
     *
     * @param orgAdmin Administrador realizando a operação
     * @param orgId    ID da organização
     * @param dto      Dados contendo o ID do treinamento e lista de IDs dos usuários
     * @return 201 Created em caso de sucesso
     * @throws AccessDeniedException   se o admin não pertencer à organização
     * @throws EntityNotFoundException se a organização não for encontrada
     */
    @PostMapping("/enrollments")
    public ResponseEntity<Void> enrollMembers(
            @AuthenticationPrincipal AuthUser orgAdmin,
            @PathVariable UUID orgId,
            @RequestBody @Valid MassEnrollmentRequest dto) {

        // Validação de segurança crucial: Garante que o admin logado pertence à organização que ele está tentando modificar.
        // FIX: Validação de segurança corrigida para usar a lógica de 'memberships'
        checkAdminPermissionForOrganization(orgAdmin, orgId);

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada."));

        enrollmentService.enrollMembersInTraining(dto.trainingId(), dto.userIds(), organization);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Endpoint para um ORG_ADMIN "adotar" um setor do catálogo global para sua organização.
     */
    @PostMapping("/sectors")
    public ResponseEntity<Void> addSectorToOrganization(
            @AuthenticationPrincipal AuthUser orgAdmin,
            @PathVariable UUID orgId,
            @RequestBody @Valid SectorIdRequest dto) {

        // FIX: Validação de segurança corrigida para usar a lógica de 'memberships'
        checkAdminPermissionForOrganization(orgAdmin, orgId);

        sectorAssignmentService.addSectorToOrganization(orgId, dto.sectorId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Lista os treinamentos do catálogo completo que estão disponíveis para a organização contratar/atribuir.
     */
    @GetMapping("/assignable-trainings")
    public ResponseEntity<List<TrainingSummaryDTO>> getAssignableTrainingsForOrg(
            @AuthenticationPrincipal AuthUser orgAdmin, // O admin que está autenticado no momento.
            @PathVariable UUID orgId) {

        // 1. A validação de segurança crucial que usa a lógica correta de 'memberships'.
        checkAdminPermissionForOrganization(orgAdmin, orgId);

        // 2. A chamada ao service, agora passando o orgId que foi validado.
        List<TrainingSummaryDTO> trainings = adminTrainingService.getAssignableTrainingsForOrg(orgId);

        return ResponseEntity.ok(trainings);
    }

    /**
     * Lista todos os setores atualmente adotados pela organização.
     *
     * @param orgAdmin Administrador realizando a consulta
     * @param orgId    ID da organização
     * @return Lista de setores da organização
     * @throws AccessDeniedException se o admin não pertencer à organização
     */
    @GetMapping("/sectors")
    public ResponseEntity<List<SectorDTO>> getOrganizationSectors(
            @AuthenticationPrincipal AuthUser orgAdmin,
            @PathVariable UUID orgId) {

        // 1. Validação de segurança para garantir que o admin pertence à organização.
        checkAdminPermissionForOrganization(orgAdmin, orgId);

        // 2. Chama o serviço para buscar a lista de setores da organização.
        //    (Este metodo 'getSectorsForOrganization' deve existir no seu SectorAssignmentService)
        List<SectorDTO> sectors = sectorAssignmentService.getSectorsForOrganization(orgId);

        return ResponseEntity.ok(sectors);
    }

    /**
     * Metodo auxiliar privado para verificar se o usuário logado é um administrador
     * da organização especificada no path.
     */
    private void checkAdminPermissionForOrganization(AuthUser admin, UUID organizationId) {
        boolean isAdminOfOrg = admin.getMemberships().stream()
                .anyMatch(membership ->
                        membership.getOrganization().getId().equals(organizationId) &&
                                "ORG_ADMIN".equals(membership.getRole().name()) // Opcional, mas recomendado
                );

        if (!isAdminOfOrg) {
            throw new AccessDeniedException("Acesso negado. Você não tem permissão para gerenciar esta organização.");
        }
    }

    /**
     * Lista todos os membros da organização que estão matriculados em um treinamento específico.
     *
     * @param orgAdmin   Administrador realizando a consulta
     * @param orgId      ID da organização
     * @param trainingId ID do treinamento
     * @return Lista de membros matriculados
     * @throws AccessDeniedException se o admin não pertencer à organização
     */
    @GetMapping("/trainings/{trainingId}/enrollments")
    public ResponseEntity<List<MemberResponseDTO>> getEnrolledMembers(
            @AuthenticationPrincipal AuthUser orgAdmin,
            @PathVariable UUID orgId,
            @PathVariable UUID trainingId) {

        // 1. Reutiliza a validação de segurança para garantir que o admin pertence à organização.
        checkAdminPermissionForOrganization(orgAdmin, orgId);

        // 2. Chama o novo método no serviço para buscar a lista de membros.
        List<MemberResponseDTO> enrolledMembers = enrollmentService.getEnrolledMembers(orgId, trainingId);

        return ResponseEntity.ok(enrolledMembers);
    }

    /**
     * Remove um setor específico da organização, deixando de "adotá-lo".
     *
     * @param orgAdmin Administrador realizando a remoção
     * @param orgId    ID da organização
     * @param sectorId ID do setor a ser removido
     * @return 204 No Content em caso de sucesso
     * @throws AccessDeniedException se o admin não pertencer à organização
     */
    @DeleteMapping("/sectors/{sectorId}")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<Void> removeSectorFromOrganization(
            @AuthenticationPrincipal AuthUser orgAdmin,
            @PathVariable UUID orgId,
            @PathVariable UUID sectorId) {

        // Validação de segurança para garantir que o admin pertence à organização.
        checkAdminPermissionForOrganization(orgAdmin, orgId);

        sectorAssignmentService.removeSectorFromOrganization(orgId, sectorId);
        return ResponseEntity.noContent().build();
    }
}