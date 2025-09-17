package com.example.docgen.auth.api;

import com.example.docgen.auth.api.dto.ProfileMeResponseDTO;
import com.example.docgen.auth.api.dto.UserProfilePFRequest;
import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.services.UserProfileService;
import com.example.docgen.enterprise.api.dto.OrganizationResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * Controller responsável por gerenciar endpoints relacionados ao perfil do usuário.
 * Esta classe contém operações como criação de perfil, consulta de informações do perfil
 * e gerenciamento de organizações relacionadas ao usuário autenticado.
 */
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Endpoint para um usuário autenticado criar seu perfil de Pessoa Física.
     *
     * @param user    O usuário autenticado, injetado pelo Spring Security.
     * @param request DTO com os dados do perfil PF.
     * @return Resposta 201 Created em caso de sucesso.
     */
    @PostMapping("/pf")
    @PreAuthorize("isAuthenticated()") // Garante que apenas usuários logados possam acessar
    public ResponseEntity<Void> createPersonalProfile(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody @Valid UserProfilePFRequest request) {

        userProfileService.createPersonalProfile(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // --- NOVO ENDPOINT PARA BUSCAR "MINHAS ORGANIZAÇÕES" ---
    @GetMapping("/me/organizations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrganizationResponseDTO>> getMyOrganizations(
            @AuthenticationPrincipal AuthUser user) {

        List<OrganizationResponseDTO> myOrganizations = userProfileService.getMyOrganizations(user);
        return ResponseEntity.ok(myOrganizations);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileMeResponseDTO> getMyProfile(@AuthenticationPrincipal AuthUser user) {
        ProfileMeResponseDTO myProfile = userProfileService.getMyProfile(user);
        return ResponseEntity.ok(myProfile);
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> requestAccountDeletion(@AuthenticationPrincipal AuthUser user) {
        userProfileService.anonymizeAndDeactivateAccount(user);
        return ResponseEntity.noContent().build();
    }

    // --- NOVO ENDPOINT DEDICADO PARA SAIR DA ORGANIZAÇÃO ---
    @DeleteMapping("/me/organizations/{organizationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> leaveOrganization(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID organizationId) {

        userProfileService.leaveOrganization(user, organizationId);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
}