package com.example.docgen.enterprise.admin;

import com.example.docgen.enterprise.admin.dto.AdminOrganizationDetailDTO;
import com.example.docgen.enterprise.admin.dto.AdminOrganizationSummaryDTO;
import com.example.docgen.enterprise.admin.dto.UpdateOrgStatusRequest;
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

@RestController
@RequestMapping("/admin/organizations")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class AdminOrganizationController {

    private final AdminOrganizationService adminOrgService;

    @GetMapping
    public ResponseEntity<List<AdminOrganizationSummaryDTO>> getAllOrganizations() {
        List<AdminOrganizationSummaryDTO> orgs = adminOrgService.getAllOrganizations().stream()
                .map(AdminOrganizationSummaryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orgs);
    }

    @GetMapping("/{organizationId}")
    public ResponseEntity<AdminOrganizationDetailDTO> getOrganizationDetails(@PathVariable UUID organizationId) {
        AdminOrganizationDetailDTO orgDetails = AdminOrganizationDetailDTO.fromEntity(
                adminOrgService.getOrganizationDetails(organizationId)
        );
        return ResponseEntity.ok(orgDetails);
    }

    @PatchMapping("/{organizationId}/status")
    public ResponseEntity<Void> updateOrganizationStatus(
            @PathVariable UUID organizationId,
            @RequestBody @Valid UpdateOrgStatusRequest request) {

        adminOrgService.updateOrganizationStatus(organizationId, request.newStatus());
        return ResponseEntity.noContent().build();
    }
}