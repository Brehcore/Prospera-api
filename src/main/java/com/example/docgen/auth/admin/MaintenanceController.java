package com.example.docgen.auth.admin;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/maintenance")
@Profile("dev") // O controlador inteiro só existe no perfil 'dev'
public class MaintenanceController {

    private final TestDataMaintenanceService maintenanceService;

    public MaintenanceController(TestDataMaintenanceService maintenanceService) {
		this.maintenanceService = maintenanceService;
	}

	@DeleteMapping("/users")
    @PreAuthorize("hasRole('ADMIN')") // Apenas usuários com role ADMIN podem executar
    public ResponseEntity<String> deleteAllUsers() {
        maintenanceService.deleteAllUsers();
        return ResponseEntity.ok("Todos os dados de usuários foram apagados com sucesso do banco de desenvolvimento.");
	}
}