package com.example.docgen.auth.domain;

import com.example.docgen.common.enums.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter // Cria todos os getters
@Setter // Cria todos os setters
@NoArgsConstructor // Cria o construtor sem argumentos
@AllArgsConstructor // Cria o construtor com todos os argumentos
@Entity
@Table(name = "tb_user_profile_pj")
public class UserProfilePJ {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private AuthUser authUser;

    @Column(unique = true, nullable = false)
    private String cnpj;

    @Column(nullable = false)
    private String razaoSocial;

    private String nomeFantasia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    private String phone;

    public UserProfilePJ(AuthUser user, String cnpj, String razaoSocial, String nomeFantasia) {

    }
}