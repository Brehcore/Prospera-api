package com.example.docgen.enterprise.service;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.repositories.AuthUserRepository;
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
    private final AuthUserRepository authUserRepository;

    @Transactional
    public Organization createOrganization(AuthUser adminUser, CreateOrganizationRequest dto) {
        // Lógica de verificação adicionada no início
        // Busca a versão "viva" do usuário para carregar suas afiliações
        AuthUser managedUser = authUserRepository.findById(adminUser.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // Verifica se o usuário já é membro de alguma organização
        if (managedUser.getMemberships() != null && !managedUser.getMemberships().isEmpty()) {
            throw new IllegalStateException("Usuários que já são membros de uma organização não podem criar uma nova.");
        }

        // A lógica de negócio existente continua a partir daqui
        if (orgRepository.existsByCnpj(dto.cnpj())) {
            throw new IllegalStateException("O CNPJ informado já está cadastrado.");
        }

        var newOrg = Organization.builder()
                .razaoSocial(dto.razaoSocial())
                .cnpj(dto.cnpj())
                .build();
        Organization savedOrg = orgRepository.save(newOrg);

        var membership = Membership.builder()
                .user(managedUser) // 3. Usa a versão gerenciada do usuário
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