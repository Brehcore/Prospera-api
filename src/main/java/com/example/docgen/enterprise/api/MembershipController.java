package com.example.docgen.enterprise.api;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.courses.api.dto.EnrollmentResponseDTO;
import com.example.docgen.enterprise.api.dto.AddMemberRequest;
import com.example.docgen.enterprise.api.dto.MemberDetailDTO;
import com.example.docgen.enterprise.api.dto.MemberResponseDTO;
import com.example.docgen.enterprise.api.dto.SectorIdRequest;
import com.example.docgen.enterprise.api.dto.UpdateMemberRoleRequest;
import com.example.docgen.enterprise.domain.Membership;
import com.example.docgen.enterprise.service.MembershipService;
import com.example.docgen.enterprise.service.SectorAssignmentService;
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

@RestController
@RequestMapping("/organizations/{organizationId}/members")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;
    private final SectorAssignmentService sectorAssignmentService;

    /**
     * Endpoint para um ORG_ADMIN adicionar um novo membro à sua organização.
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

    // --- NOVO ENDPOINT PARA LISTAR MEMBROS ---
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

    // --- NOVO ENDPOINT PARA REMOVER UM MEMBRO ---
    @DeleteMapping("/{membershipId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeMember(
            @AuthenticationPrincipal AuthUser currentUser,
            @PathVariable UUID organizationId,
            @PathVariable UUID membershipId) {

        membershipService.removeMember(currentUser, organizationId, membershipId);
        return ResponseEntity.noContent().build(); // 204 No Content é o status ideal para delete
    }

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
     * Endpoint para um ORG_ADMIN atribuir um setor a um membro da sua organização.
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
     * Retorna o progresso de um membro específico em todos os seus treinamentos.
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

    @GetMapping("/{membershipId}")
    public ResponseEntity<MemberDetailDTO> getMemberDetails(
            @AuthenticationPrincipal AuthUser orgAdmin,
            @PathVariable UUID organizationId,
            @PathVariable UUID membershipId) {

        MemberDetailDTO details = membershipService.getMemberDetails(orgAdmin, organizationId, membershipId);
        return ResponseEntity.ok(details);
    }



}