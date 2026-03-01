package com.example.prospera.admin.controllers;

import com.example.prospera.admin.dto.AdminResetPasswordRequest;
import com.example.prospera.admin.services.AdminUserService;
import com.example.prospera.admin.services.TestDataMaintenanceService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Este controlador oferece endpoints para operações administrativas de manutenção
 * exclusivamente no ambiente de desenvolvimento ('local' profile).
 * Ele contém funcionalidades como limpar todos os usuários do banco de dados
 * e redefinir senhas de usuários administradores.
 */
@RestController
@RequestMapping("/admin/maintenance")
@Profile("local")
public class MaintenanceController {

    private final TestDataMaintenanceService maintenanceService;
    private final AdminUserService adminUserService;

    public MaintenanceController(TestDataMaintenanceService maintenanceService, AdminUserService adminUserService) {
		this.maintenanceService = maintenanceService;
        this.adminUserService = adminUserService;
	}

    /**
     * Este metodo apaga todos os dados de usuários do banco de desenvolvimento.
     * Apenas usuários com a role 'ADMIN' têm permissão para executar este metodo.
     *
     * @return ResponseEntity com uma mensagem de sucesso ao apagar os dados.
     */
    @DeleteMapping("/users")
    @PreAuthorize("hasRole('ADMIN')") // Apenas usuários com role ADMIN podem executar
    public ResponseEntity<String> deleteAllUsers() {
        maintenanceService.deleteAllUsers();
        return ResponseEntity.ok("Todos os dados de usuários foram apagados com sucesso do banco de desenvolvimento.");
    }

    /**
     * Este metodo redefine a senha de um usuário administrador com base no e-mail fornecido.
     * Apenas usuários com a role 'SYSTEM_ADMIN' têm permissão para executar este metodo.
     *
     * @param request Objeto contendo o e-mail do usuário e a nova senha.
     * @return ResponseEntity com uma mensagem de sucesso ao redefinir a senha.
     */
    @PostMapping("/reset-password")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid AdminResetPasswordRequest request) {
        adminUserService.adminResetPassword(request.email(), request.newPassword());
        return ResponseEntity.ok("Senha para o usuário " + request.email() + " resetada com sucesso.");
    }
}