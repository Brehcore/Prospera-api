package com.example.docgen.enterprise.api;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.enterprise.api.dto.CreateOrganizationRequest;
import com.example.docgen.enterprise.api.dto.OrganizationResponseDTO; // Importe o novo DTO
import com.example.docgen.enterprise.domain.Organization; // Importe a entidade
import com.example.docgen.enterprise.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService orgService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrganizationResponseDTO> createOrganization( // O tipo de retorno agora é o DTO
                                                                       @AuthenticationPrincipal AuthUser user,
                                                                       @RequestBody @Valid CreateOrganizationRequest dto) {

        // 1. Chama o serviço e captura a nova organização criada
        Organization newOrganization = orgService.createOrganization(user, dto);

        // 2. Cria a URL para o novo recurso (boa prática REST)
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newOrganization.getId())
                .toUri();

        // 3. Converte a entidade para o DTO de resposta e retorna no corpo
        OrganizationResponseDTO responseDTO = OrganizationResponseDTO.fromEntity(newOrganization);

        return ResponseEntity.created(location).body(responseDTO);
    }

    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')") // Apenas usuários logados podem ver
    public ResponseEntity<List<OrganizationResponseDTO>> getAllOrganizations() {
        List<OrganizationResponseDTO> organizations = orgService.getAllOrganizations()
                .stream()
                .map(OrganizationResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(organizations);
    }
}