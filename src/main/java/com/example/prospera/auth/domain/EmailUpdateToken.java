package com.example.prospera.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "email_update_tokens")
public class EmailUpdateToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String verificationCode; // O código de 6 dígitos (ex: "482910")

    @Column(nullable = false)
    private String newPendingEmail; // O e-mail novo que ele QUER usar

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // Construtor utilitário
    public EmailUpdateToken(String code, String newPendingEmail, AuthUser user) {
        this.verificationCode = code;
        this.newPendingEmail = newPendingEmail;
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusMinutes(15); // Validade curta (15 min)
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}