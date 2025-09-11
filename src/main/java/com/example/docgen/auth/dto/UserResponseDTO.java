package com.example.docgen.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para a resposta do endpoint de registro.
 * Contém apenas os dados essenciais da identidade recém-criada.
 * Usa Lombok para eliminar código repetitivo.
 */
@Data // Anotação do Lombok que cria getters, setters, toString, equals e hashCode
@NoArgsConstructor // Cria o construtor vazio
@AllArgsConstructor // Cria o construtor com todos os campos
public class UserResponseDTO {

    private UUID id;
	private String email;
	private String role;

}