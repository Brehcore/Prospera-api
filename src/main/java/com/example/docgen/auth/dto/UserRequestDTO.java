package com.example.docgen.auth.dto;

import com.example.docgen.common.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Gera getters, setters, toString, etc.
@NoArgsConstructor // Gera o construtor vazio
@AllArgsConstructor // Gera o construtor com todos os campos
public class UserRequestDTO {

	@NotBlank(message = "Seu Nome é obrigatório.")
	private String name;

	@NotBlank(message = "Seu email é obrigatório.")
	private String email;

	@NotBlank(message = "Sua senha é obrigatória.")
	@Size(min = 8, message = "Sua senha deve ter no mínimo 8 caracteres.")
	private String password;

    @Size(min = 8, max = 20, message = "Telefone deve ter entre 8 e 20 caracteres.")
	@NotBlank(message = "Seu telefone é obrigatório.")
	private String phone;

    @NotNull(message = "O tipo de usuário é obrigatório.")
    private UserType userType;
}