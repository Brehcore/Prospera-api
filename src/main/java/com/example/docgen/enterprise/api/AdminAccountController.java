package com.example.docgen.enterprise.api;

import com.example.docgen.enterprise.api.dto.AccountSummaryDTO;
import com.example.docgen.enterprise.domain.Account;
import com.example.docgen.enterprise.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')") // Protege todos os endpoints da classe
public class AdminAccountController {

    private final AccountService accountService;

    /**
     * Endpoint para listar todas as Contas (clientes) cadastradas no sistema.
     *
     * @return Uma lista com o ID e o nome de cada conta.
     */
    @GetMapping
    public ResponseEntity<List<AccountSummaryDTO>> listAllAccounts() {
        // 1. Chama o serviço para buscar a lista de entidades Account do banco.
        List<Account> accounts = accountService.findAllAccounts();

        // 2. Mapeia a lista de entidades para a lista de DTOs.
        List<AccountSummaryDTO> response = accounts.stream()
                .map(AccountSummaryDTO::fromEntity)
                .collect(Collectors.toList());

        // 3. Retorna 200 OK com a lista de DTOs no corpo da resposta.
        return ResponseEntity.ok(response);
    }
}