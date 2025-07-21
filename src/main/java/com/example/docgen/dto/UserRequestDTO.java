package com.example.docgen.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UserRequestDTO {

	@NotBlank(message = "Seu Nome é obrigatório")
	private String name;

	@NotBlank(message = "Seu email é obrigatório")
	private String email;

	@NotBlank(message = "Sua senha é obrigatório")
	@Size(min = 8, message = "Sua senha deve ter no minimo 8 caracteres")
	private String password;

	@NotNull(message = "Sua idade é obrigatória")
	@Past(message = "A data de nascimento deve ser no passado")
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate birthDate;

	@Size(min = 8, max = 20, message = "Telefone deve ter entre 8 e 20 caracteres")
	@NotBlank(message = "Seu telefone é obrigatório")
	private String phone;

	
	private String cpf;
	
	
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}
	
	
	
	

}