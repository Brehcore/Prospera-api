package com.example.prospera.enterprise.controllers;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.enterprise.domain.Organization;
import com.example.prospera.enterprise.dto.CreateOrganizationRequest;
import com.example.prospera.enterprise.dto.OrganizationResponseDTO;
import com.example.prospera.enterprise.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Controller responsável por gerenciar a criação e consulta de organizações.
 * <p>
 * Esta classe define endpoints relacionados as operações CRUD em organizações,
 * como criar uma nova organização e listar todas as organizações existentes.
 */
@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {


    private final OrganizationService orgService;

    /**
     * Cria uma nova organização.
     *
     * <p>Endpoint que permite a criação de uma nova organização associada ao
     * usuário autenticado. Os dados da organização devem ser fornecidos através do corpo da requisição.</p>
     *
     * @param user Usuário autenticado que está criando a organização.
     * @param dto  Dados da organização a ser criada.
     * @return Resposta contendo o DTO da organização criada e o status 201 Created.
     */
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
}