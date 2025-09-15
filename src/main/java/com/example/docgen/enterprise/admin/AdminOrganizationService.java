package com.example.docgen.enterprise.admin;

import com.example.docgen.enterprise.domain.Organization;
import com.example.docgen.enterprise.repositories.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminOrganizationService {

    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll(Sort.by("razaoSocial"));
    }

    @Transactional(readOnly = true)
    public Organization getOrganizationDetails(UUID organizationId) {
        // Usamos uma query com JOIN FETCH para carregar os membros junto
        return organizationRepository.findWithMembersById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organização não encontrada: " + organizationId));
    }

    // Futuramente, você pode adicionar o método de ativar/inativar aqui
    // @Transactional
    // public void updateOrganizationStatus(UUID organizationId, boolean isActive) { ... }
}