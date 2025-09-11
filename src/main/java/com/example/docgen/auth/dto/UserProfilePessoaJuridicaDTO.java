package com.example.docgen.auth.dto;

import com.example.docgen.common.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para receber os dados de um perfil de Pessoa Jurídica.
 * Contém validações para garantir a integridade dos dados de entrada.
 */
public class UserProfilePessoaJuridicaDTO {

    @NotBlank(message = "O CNPJ é obrigatório.")
    private String cnpj;

    @NotBlank(message = "A razão social é obrigatória.")
    private String razaoSocial;

    private String nomeFantasia; // Opcional

    // CAMPO ADICIONADO
    @NotNull(message = "O tipo de usuário é obrigatório.")
    private UserType userType;

    // CAMPO ADICIONADO (opcional)
    private String phone;

    // Getters e Setters
    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
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