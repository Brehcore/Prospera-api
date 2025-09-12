package com.example.docgen.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "auth_user_profile_pf")
public class UserProfilePF {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id")
    private AuthUser user;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false, length = 11)
    private String cpf;

    private LocalDate birthDate;
    private String phone;
}