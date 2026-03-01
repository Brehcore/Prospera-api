package com.example.prospera.admin.controllers;

import com.example.prospera.admin.services.AdminAccountService;
import com.example.prospera.enterprise.domain.Account;
import com.example.prospera.enterprise.dto.AccountSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller responsável por operações administrativas relacionadas a contas de usuário.
 * Requer autenticação com papel SYSTEM_ADMIN para acesso aos endpoints.
 */
@RestController
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AdminAccountController {

    private final AdminAccountService AdminAccountService;

    /**
     * Lista todas as contas de usuário cadastradas no sistema.
     *
     * @return ResponseEntity contendo uma lista de {@link AccountSummaryDTO} com o resumo das contas
     * e status HTTP 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<AccountSummaryDTO>> listAllAccounts() {
        // 1. Chama o serviço para buscar a lista de entidades Account do banco.
        List<Account> accounts = AdminAccountService.findAllAccounts();

        // 2. Mapeia a lista de entidades para a lista de DTOs.
        List<AccountSummaryDTO> response = accounts.stream()
                .map(AccountSummaryDTO::fromEntity)
                .collect(Collectors.toList());

        // 3. Retorna 200 OK com a lista de DTOs no corpo da resposta.
        return ResponseEntity.ok(response);
    }
}