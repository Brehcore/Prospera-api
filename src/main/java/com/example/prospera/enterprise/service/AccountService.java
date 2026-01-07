package com.example.prospera.enterprise.service;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.auth.repositories.AuthUserRepository;
import com.example.prospera.enterprise.domain.Account;
import com.example.prospera.enterprise.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuthUserRepository authUserRepository;

    /**
     * Encontra a conta pessoal de um usuário. Se não existir, cria uma nova.
     * Este metodo garante que cada usuário individual tenha uma conta para
     * atrelar suas assinaturas.
     *
     * @param user O usuário para o qual a conta será encontrada ou criada.
     * @return A conta pessoal do usuário, seja ela existente ou recém-criada.
     */
    @Transactional
    public Account findOrCreatePersonalAccountForUser(AuthUser user) {
        // Verifica se o usuário já tem uma conta pessoal vinculada
        if (user.getPersonalAccount() != null) {
            return user.getPersonalAccount();
        }

        // Se não tiver, cria uma nova conta
        // O nome da conta pode ser o e-mail ou nome do usuário para fácil identificação
        Account newPersonalAccount = new Account(user.getEmail());
        Account savedAccount = accountRepository.save(newPersonalAccount);

        // Vincula a nova conta ao usuário para futuras consultas serem mais rápidas
        user.setPersonalAccount(savedAccount);
        authUserRepository.save(user);

        return savedAccount;
    }

    @Transactional(readOnly = true)
    public List<Account> findAllAccounts() {
        return accountRepository.findAll();
    }
}
