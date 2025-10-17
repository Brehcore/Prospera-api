package com.example.docgen.auth.domain;

import com.example.docgen.common.enums.UserRole;
import com.example.docgen.enterprise.domain.Account;
import com.example.docgen.enterprise.domain.Membership;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "auth_users")
public class AuthUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfilePF personalProfile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Membership> memberships;

    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * A conta pessoal deste usuário, usada para assinaturas individuais.
     * AuthUser é o "dono" da relação.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "personal_account_id", referencedColumnName = "id")
    private Account personalAccount;


    public AuthUser(String email, String password, UserRole role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 1. Se o usuário tem um vínculo com uma organização (Membership)...
        if (this.memberships != null && !this.memberships.isEmpty()) {
            // ... as permissões dele vêm da role DENTRO da organização (ORG_ADMIN, ORG_MEMBER)
            return this.memberships.stream()
                    .map(membership -> new SimpleGrantedAuthority("ROLE_" + membership.getRole().name()))
                    .collect(Collectors.toList());
        }

        // 2. Se não, ele é um usuário avulso (PF), então usamos a role base.
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}