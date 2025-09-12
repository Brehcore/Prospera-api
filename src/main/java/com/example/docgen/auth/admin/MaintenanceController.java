package com.example.docgen.auth.admin;

import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/maintenance")
@Profile("dev") // O controlador inteiro só existe no perfil 'dev'
public class MaintenanceController {

    private final TestDataMaintenanceService maintenanceService;
    private final AdminUserService adminUserService;

    public MaintenanceController(TestDataMaintenanceService maintenanceService, AdminUserService adminUserService) {
		this.maintenanceService = maintenanceService;
        this.adminUserService = adminUserService;
	}

	@DeleteMapping("/users")
    @PreAuthorize("hasRole('ADMIN')") // Apenas usuários com role ADMIN podem executar
    public ResponseEntity<String> deleteAllUsers() {
        maintenanceService.deleteAllUsers();
        return ResponseEntity.ok("Todos os dados de usuários foram apagados com sucesso do banco de desenvolvimento.");
	}

    @PostMapping("/reset-password")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid AdminResetPasswordRequest request) {
        adminUserService.adminResetPassword(request.email(), request.newPassword());
        return ResponseEntity.ok("Senha para o usuário " + request.email() + " resetada com sucesso.");
    }
}