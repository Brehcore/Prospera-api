package com.example.prospera.enterprise.service;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.auth.exceptions.BusinessRuleException;
import com.example.prospera.auth.repositories.AuthUserRepository;
import com.example.prospera.common.enums.OrganizationRole;
import com.example.prospera.common.service.AuthorizationService;
import com.example.prospera.common.validation.CnpjValidationService;
import com.example.prospera.enterprise.api.dto.CreateOrganizationRequest;
import com.example.prospera.enterprise.domain.Account;
import com.example.prospera.enterprise.domain.Membership;
import com.example.prospera.enterprise.domain.Organization;
import com.example.prospera.enterprise.repositories.AccountRepository;
import com.example.prospera.enterprise.repositories.MembershipRepository;
import com.example.prospera.enterprise.repositories.OrganizationRepository;
import com.example.prospera.subscription.entities.Plan;
import com.example.prospera.subscription.entities.Subscription;
import com.example.prospera.subscription.enums.PlanType;
import com.example.prospera.subscription.enums.SubscriptionStatus;
import com.example.prospera.subscription.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository orgRepository;
    private final MembershipRepository membershipRepository;
    private final AuthUserRepository authUserRepository;
    private final CnpjValidationService cnpjValidationService;
    private final SubscriptionRepository subscriptionRepository;
    private final AccountRepository accountRepository;
    private final AuthorizationService authorizationService;

    @Transactional
    public Organization createOrganization(AuthUser adminUser, CreateOrganizationRequest dto) {
        if (adminUser.getPersonalAccount() != null) {
            Optional<Subscription> activePersonalSub = subscriptionRepository.findByAccountAndStatus(
                    adminUser.getPersonalAccount(),
                    SubscriptionStatus.ACTIVE
            );
            if (activePersonalSub.isPresent()) {
                Plan userPlan = activePersonalSub.get().getPlan();
                if (userPlan.getType() == PlanType.INDIVIDUAL) {
                    throw new BusinessRuleException(
                            "Você possui uma assinatura individual ativa. Cancele-a e aguarde o término para poder criar uma organização."
                    );
                }
            }
        }

        String cleanedCnpj = dto.cnpj().replaceAll("[^\\d]", "");
        cnpjValidationService.validate(cleanedCnpj);

        if (orgRepository.existsByCnpj(cleanedCnpj)) {
            throw new IllegalStateException("O CNPJ informado já está cadastrado.");
        }

        AuthUser managedUser = authUserRepository.findById(adminUser.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        Account targetAccount;
        boolean isFirstOrganization = (managedUser.getMemberships() == null || managedUser.getMemberships().isEmpty());

        if (isFirstOrganization) {
            Account newAccount = new Account(dto.razaoSocial());
            targetAccount = accountRepository.save(newAccount);
        } else {
            Account existingAccount = managedUser.getMemberships().stream()
                    .findFirst()
                    .map(membership -> membership.getOrganization().getAccount())
                    .orElseThrow(() -> new IllegalStateException("Não foi possível encontrar a conta da sua organização existente."));

            // A chamada agora funciona, pois o serviço está injetado.
            authorizationService.checkIsAdminOfAccount(managedUser, existingAccount.getId());
            targetAccount = existingAccount;
        }

        var newOrg = Organization.builder()
                .razaoSocial(dto.razaoSocial())
                .cnpj(cleanedCnpj)
                .account(targetAccount)
                .build();
        Organization savedOrg = orgRepository.save(newOrg);

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