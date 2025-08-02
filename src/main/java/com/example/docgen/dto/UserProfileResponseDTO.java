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
    private String message; // Para mensagens de erro ou sucesso adicionais
    private User userDetails; // Opcional: Para retornar o objeto User completo se preferir
    private String role; // Incluir a role do usuário

    // Construtor para sucesso (retornando apenas dados específicos)
    public UserProfileResponseDTO(String name, String email, String phone, String role) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    // Construtor para sucesso (retornando o objeto User diretamente, se preferir)
    public UserProfileResponseDTO(User user) {
        if (user != null) {
            this.name = user.getName();
            this.email = user.getEmail();
            this.phone = user.getPhone();
            this.role = user.getRole().name();
            this.userDetails = user; // Opcional: inclui o objeto User completo
        }
    }

    // Construtor para erro
    public UserProfileResponseDTO(String message) {
        this.message = message;
    }
}