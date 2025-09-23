package com.example.docgen.auth.admin;

import com.example.docgen.auth.admin.dto.AdminUserDetailDTO;
import com.example.docgen.auth.admin.dto.AdminUserSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<AdminUserSummaryDTO>> getAllUsers(
            @RequestParam(required = false) String email) {

        List<AdminUserSummaryDTO> users = adminUserService.getAllUsers(email).stream()
                .map(AdminUserSummaryDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDetailDTO> getUserDetails(@PathVariable UUID userId) {
        AdminUserDetailDTO userDetails = AdminUserDetailDTO.fromEntity(
                adminUserService.getUserDetails(userId)
        );
        return ResponseEntity.ok(userDetails);
    }

    // --- RECOMENDAÇÃO 3: ENDPOINT PARA DESATIVAR UM USUÁRIO ---
    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable UUID userId) {
        adminUserService.deactivateUser(userId);
        return ResponseEntity.ok("Usuário desativado com sucesso.");
    }

    // --- RECOMENDAÇÃO 4: ENDPOINT PARA ATIVAR UM USUÁRIO ---
    @PatchMapping("/{userId}/activate")
    public ResponseEntity<String> activateUser(@PathVariable UUID userId) {
        adminUserService.activateUser(userId);
        return ResponseEntity.ok("Usuário ativado com sucesso.");
    }
}