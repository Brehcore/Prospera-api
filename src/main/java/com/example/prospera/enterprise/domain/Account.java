package com.example.prospera.enterprise.domain;

import com.example.prospera.auth.domain.AuthUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor // JPA exige um construtor sem argumentos
@Entity
@Table(name = "accounts")
public class Account {

    /**
     * Identificador único da Conta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Nome da Conta (ex: "Grupo Empresarial XYZ").
     * Útil para identificar o cliente dono da assinatura.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Uma Conta pode possuir uma ou mais Organizações.
     * Esta relação define o agrupamento de empresas sob a mesma assinatura.
     */
    @OneToMany(
            mappedBy = "account", // Corresponde ao campo 'account' na entidade Organization
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Organization> organizations = new ArrayList<>();

    /**
     * Se esta for uma conta pessoal, este campo aponta para o usuário dono.
     * 'mappedBy' indica que a entidade 'AuthUser' é a dona do relacionamento,
     * através do campo 'personalAccount'.
     */
    @OneToOne(mappedBy = "personalAccount", fetch = FetchType.LAZY)
    private AuthUser personalUser;

    // Construtor de conveniência (opcional)
    public Account(String name) {
        this.name = name;
    }
}