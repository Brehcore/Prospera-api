package com.example.docgen.auth.dto;

import com.example.docgen.common.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO para receber os dados de um perfil de Pessoa Física.
 * Contém validações para garantir a integridade dos dados de entrada.
 */
public class UserProfilePessoaFisicaDTO {

    @NotBlank(message = "O CPF é obrigatório.")
    private String cpf;

    // CAMPO ADICIONADO
    @NotBlank(message = "O nome completo é obrigatório.")
    private String fullName;

    @NotNull(message = "A data de nascimento é obrigatória.")
    private LocalDate birthDate;

    // CAMPO ADICIONADO
    @NotNull(message = "O tipo de usuário é obrigatório.")
    private UserType userType;

    // CAMPO ADICIONADO (opcional)
    private String phone;

    // Getters e Setters
    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}