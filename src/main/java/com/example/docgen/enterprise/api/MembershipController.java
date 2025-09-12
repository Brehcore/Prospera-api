package com.example.docgen.enterprise.api;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.enterprise.api.dto.AddMemberRequest;
import com.example.docgen.enterprise.api.dto.MemberResponseDTO;
import com.example.docgen.enterprise.api.dto.UpdateMemberRoleRequest;
import com.example.docgen.enterprise.domain.Membership;
import com.example.docgen.enterprise.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/organizations/{organizationId}/members")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    /**
     * Endpoint para um ORG_ADMIN adicionar um novo membro à sua organização.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addMember(
            @AuthenticationPrincipal AuthUser adminUser,
            @PathVariable UUID organizationId,
            @RequestBody @Valid AddMemberRequest request) {

        membershipService.addMemberToOrganization(adminUser, organizationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
}