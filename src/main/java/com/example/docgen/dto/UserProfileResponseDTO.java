package com.example.docgen.dto;

import com.example.docgen.entities.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDTO {
    private String name;
    private String email;
    private String phone;
    private String message; // Para mensagens de erro ou sucesso
    private String role; // Incluir a role do usuário

    // Construtor para sucesso
    public UserProfileResponseDTO(String name, String email, String phone, String role) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    // Construtor para sucesso, pegando os dados da entidade User
    public UserProfileResponseDTO(User user) {
        if (user != null) {
            this.name = user.getName();
            this.email = user.getEmail();
            this.phone = user.getPhone();
            this.role = user.getRole().name();
        }
    }

    // Construtor para mensagens de erro/sucesso
    public UserProfileResponseDTO(String message) {
        this.message = message;
    }
}