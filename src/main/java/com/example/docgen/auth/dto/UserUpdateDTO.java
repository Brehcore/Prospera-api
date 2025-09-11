package com.example.docgen.auth.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para receber dados na atualização de um perfil de usuário.
 * Todos os campos são opcionais, permitindo a atualização parcial.
 */
@Data
public class UserUpdateDTO {

    // CORREÇÃO: min = 3, para corresponder à mensagem de erro.
    @Size(min = 3, max = 50, message = "Seu nome deve ter entre 3 e 50 caracteres")
	private String name;

	@Size(min = 8, max = 20, message = "Telefone deve ter entre 8 e 20 caracteres")
	private String phone;
}