package com.example.docgen.enterprise.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "organizations")
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String razaoSocial;

    @Column(unique = true, nullable = false, length = 14)
    private String cnpj;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<Membership> memberships;
}