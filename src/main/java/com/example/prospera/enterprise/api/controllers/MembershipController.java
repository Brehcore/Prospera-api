package com.example.prospera.enterprise.api.controllers;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.courses.api.dto.EnrollmentResponseDTO;
import com.example.prospera.enterprise.api.dto.AddMemberRequest;
import com.example.prospera.enterprise.api.dto.MemberDetailDTO;
import com.example.prospera.enterprise.api.dto.MemberResponseDTO;
import com.example.prospera.enterprise.api.dto.SectorIdRequest;
import com.example.prospera.enterprise.api.dto.UpdateMemberRoleRequest;
import com.example.prospera.enterprise.domain.Membership;
import com.example.prospera.enterprise.service.MembershipService;
import com.example.prospera.enterprise.service.SectorAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller responsável por gerenciar membros de uma organização, incluindo adição,
 * remoção, atualização de papéis e atribuição de setores.
 */
@RestController
@RequestMapping("/organizations/{organizationId}/members")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;
    private final SectorAssignmentService sectorAssignmentService;

    /**
     * Adiciona um novo membro à organização.
     *
     * @param adminUser      Usuário autenticado que está realizando a operação
     * @param organizationId ID da organização
     * @param request        Dados do novo membro a ser adicionado
     * @return Dados do membro recém-adicionado
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    // Passo 1: Mude o tipo de retorno para o DTO de resposta.
    public ResponseEntity<MemberResponseDTO> addMember(
            @AuthenticationPrincipal AuthUser adminUser,
            @PathVariable UUID organizationId,
            @RequestBody @Valid AddMemberRequest request) {

        // Passo 2: Capture o objeto Membership que o serviço retorna.
        Membership newMembership = membershipService.addMemberToOrganization(adminUser, organizationId, request);

        // Passo 3: Converta para DTO e envie-o no corpo da resposta com o status 201 Created.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MemberResponseDTO.fromEntity(newMembership));
    }

    /**
     * Lista todos os membros de uma organização.
     *
     * @param currentUser    Usuário autenticado que está realizando a consulta
     * @param organizationId ID da organização
     * @return Lista de membros da organização
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MemberResponseDTO>> listMembers(
            @AuthenticationPrincipal AuthUser currentUser,
            @PathVariable UUID organizationId) {

        // A conversão para DTO agora acontece aqui, no controller
        List<Membership> members = membershipService.listMembers(currentUser, organizationId);
        List<MemberResponseDTO> response = members.stream()
                .map(MemberResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Remove um membro da organização.
     *
     * @param currentUser    Usuário autenticado que está realizando a remoção
     * @param organizationId ID da organização
     * @param membershipId   ID da associação do membro
     * @return Resposta sem conteúdo (204)
     */
    @DeleteMapping("/{membershipId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeMember(
            @AuthenticationPrincipal AuthUser currentUser,
            @PathVariable UUID organizationId,
            @PathVariable UUID membershipId) {

        membershipService.removeMember(currentUser, organizationId, membershipId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Atualiza o papel (role) de um membro na organização.
     *
     * @param currentUser    Usuário autenticado que está realizando a atualização
     * @param organizationId ID da organização
     * @param membershipId   ID da associação do membro
     * @param request        Novo papel a ser atribuído
     * @return Dados atualizados do membro
     */
    @PatchMapping("/{membershipId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MemberResponseDTO> updateMemberRole(
            @AuthenticationPrincipal AuthUser currentUser,
            @PathVariable UUID organizationId,
            @PathVariable UUID membershipId,
            @RequestBody @Valid UpdateMemberRoleRequest request) {

        Membership updatedMembership = membershipService.updateMemberRole(currentUser, organizationId, membershipId, request.newRole());
        return ResponseEntity.ok(MemberResponseDTO.fromEntity(updatedMembership));
    }

    /**
     * Atribui um setor a um membro da organização.
     *
     * @param adminUser      Administrador realizando a atribuição
     * @param organizationId ID da organização
     * @param membershipId   ID da associação do membro
     * @param request        ID do setor a ser atribuído
     * @return Resposta de criação sem conteúdo (201)
     */
    @PostMapping("/{membershipId}/sectors")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<Void> assignSectorToMember(
            @AuthenticationPrincipal AuthUser adminUser,
            @PathVariable UUID organizationId,
            @PathVariable UUID membershipId, // Usar membershipId é mais específico que userId
            @RequestBody @Valid SectorIdRequest request) {

        // O Service precisa ser ajustado para receber o membershipId
        // e validar se o adminUser pertence à organizationId
        sectorAssignmentService.assignSectorToMember(adminUser, organizationId, membershipId, request.sectorId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Obtém o progresso de um membro nos cursos da organização.
     *
     * @param orgAdmin       Administrador consultando o progresso
     * @param organizationId ID da organização
     * @param membershipId   ID da associação do membro
     * @return Lista de matrículas com progresso do membro
     */
    @GetMapping("/{membershipId}/progress")
    public ResponseEntity<List<EnrollmentResponseDTO>> getMemberProgress(
            @AuthenticationPrincipal AuthUser orgAdmin,
            @PathVariable UUID organizationId,
            @PathVariable UUID membershipId) {

        // A validação de permissão deve ser feita no serviço
        List<EnrollmentResponseDTO> progress = membershipService.getMemberEnrollmentProgress(orgAdmin, organizationId, membershipId);
        return ResponseEntity.ok(progress);
    }

    /**
     * Obtém detalhes completos de um membro específico.
     *
     * @param orgAdmin       Administrador consultando os detalhes
     * @param organizationId ID da organização
     * @param membershipId   ID da associação do membro
     * @return Detalhes completos do membro
     */
    @GetMapping("/{membershipId}")
    public ResponseEntity<MemberDetailDTO> getMemberDetails(
            @AuthenticationPrincipal AuthUser orgAdmin,
            @PathVariable UUID organizationId,
            @PathVariable UUID membershipId) {

        MemberDetailDTO details = membershipService.getMemberDetails(orgAdmin, organizationId, membershipId);
        return ResponseEntity.ok(details);
    }
}