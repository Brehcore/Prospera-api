package com.example.docgen.enterprise.service;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.repositories.AuthUserRepository;
import com.example.docgen.common.enums.OrganizationRole;
import com.example.docgen.common.validation.CnpjValidationService;
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
    private final CnpjValidationService cnpjValidationService;

    @Transactional
    public Organization createOrganization(AuthUser adminUser, CreateOrganizationRequest dto) {
        // 1. Limpe o CNPJ PRIMEIRO, removendo a máscara.
        String cleanedCnpj = dto.cnpj().replaceAll("[^\\d]", "");

        // 2. Valide o CNPJ já limpo.
        cnpjValidationService.validate(cleanedCnpj);

        // 3. Verificação de existência (apenas uma vez, com o CNPJ limpo).
        if (orgRepository.existsByCnpj(cleanedCnpj)) {
            throw new IllegalStateException("O CNPJ informado já está cadastrado.");
        }

        // 4. Lógica de negócio do usuário.
        AuthUser managedUser = authUserRepository.findById(adminUser.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (managedUser.getMemberships() != null && !managedUser.getMemberships().isEmpty()) {
            throw new IllegalStateException("Usuários que já são membros de uma organização não podem criar uma nova.");
        }

        // 5. Use APENAS o CNPJ limpo para criar a nova organização.
        var newOrg = Organization.builder()
                .razaoSocial(dto.razaoSocial())
                .cnpj(cleanedCnpj) // Apenas a versão limpa
                .build();
        Organization savedOrg = orgRepository.save(newOrg);

        // 6. Crie a afiliação (membership).
        var membership = Membership.builder()
                .user(managedUser)
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