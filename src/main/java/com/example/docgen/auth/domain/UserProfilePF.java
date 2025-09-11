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

import java.time.LocalDate;
import java.util.UUID;

@Getter // Cria todos os getters
@Setter // Cria todos os setters
@NoArgsConstructor // Cria o construtor sem argumentos -> public UserProfilePF() {}
@AllArgsConstructor // Cria o construtor com todos os argumentos
@Entity
@Table(name = "tb_user_profile_pf")
public class UserProfilePF {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private AuthUser authUser;

    @Column(unique = true, nullable = false)
    private String cpf;

    @Column(nullable = false)
    private String fullName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    private String phone;

    public UserProfilePF(AuthUser user, String cpf, String fullName, LocalDate birthDate) {
    }
}