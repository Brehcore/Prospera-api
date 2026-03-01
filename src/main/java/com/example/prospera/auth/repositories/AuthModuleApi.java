package com.example.prospera.auth.repositories;

import com.example.prospera.auth.dto.UserDTO;

import java.util.Optional;
import java.util.UUID;

/**
 * Contrato público do módulo de Autenticação.
 * Outros módulos devem usar APENAS esta interface para interagir com os dados de usuários.
 */
public interface AuthModuleApi {

    /**
     * Busca um usuário pelo seu ID e retorna seus dados públicos em um DTO.
     * A 'role' do usuário já está incluída no UserDTO.
     */
    Optional<UserDTO> findUserById(UUID userId);

    /**
     * Verifica se um usuário possui uma permissão específica para executar uma ação.
     * Útil para controle de acesso refinado.
     */
    boolean hasPermission(UUID userId, String permission);

}