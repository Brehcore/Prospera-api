package com.example.prospera.certificate.domain;

import com.example.prospera.courses.domain.Enrollment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Vínculo 1-para-1 com a matrícula. Uma matrícula gera um certificado.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    private Enrollment enrollment;

    // Código único para validação externa (anti-fraude)
    @Column(nullable = false, unique = true)
    private String validationCode;

    // Caminho do arquivo PDF salvo no FileStorageService
    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private OffsetDateTime issuedAt;

    @PrePersist
    void prePersist() {
        issuedAt = OffsetDateTime.now();
        if (validationCode == null) {
            validationCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}