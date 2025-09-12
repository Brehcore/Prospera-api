package com.example.docgen.enterprise.service;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.common.enums.OrganizationRole;
import com.example.docgen.enterprise.api.dto.CreateOrganizationRequest;
import com.example.docgen.enterprise.domain.Membership;
import com.example.docgen.enterprise.domain.Organization;
import com.example.docgen.enterprise.repositories.MembershipRepository;
import com.example.docgen.enterprise.repositories.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository orgRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public Organization createOrganization(AuthUser adminUser, CreateOrganizationRequest dto) {
        // ... Lógica para validar CNPJ, etc. ...
        if (orgRepository.existsByCnpj(dto.cnpj())) {
            throw new IllegalStateException("O CNPJ informado já está cadastrado.");
        }

        var newOrg = Organization.builder()
                .razaoSocial(dto.razaoSocial())
                .cnpj(dto.cnpj())
                .build();
        Organization savedOrg = orgRepository.save(newOrg);

        // Cria a afiliação, tornando o usuário o ADMIN da nova organização
        var membership = Membership.builder()
                .user(adminUser)
                .organization(savedOrg)
                .role(OrganizationRole.ORG_ADMIN)
                .build();
        membershipRepository.save(membership);

        return savedOrg;
    }

    @Transactional(readOnly = true)
    public List<Organization> getAllOrganizations() {
        return orgRepository.findAll();
    }
}