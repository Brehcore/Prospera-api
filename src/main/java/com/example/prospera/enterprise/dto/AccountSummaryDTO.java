package com.example.prospera.enterprise.dto;

import com.example.prospera.enterprise.domain.Account;

import java.util.UUID;

/**
 * DTO para uma visão resumida de uma Conta (cliente),
 * ideal para listagens administrativas.
 */
public record AccountSummaryDTO(
        UUID id,
        String name
) {
    /**
     * Metodo estático para converter a entidade Account para este DTO.
     */
    public static AccountSummaryDTO fromEntity(Account account) {
        return new AccountSummaryDTO(
                account.getId(),
                account.getName()
        );
    }
}