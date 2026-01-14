package com.example.prospera.certificate.repositories;

import com.example.prospera.certificate.domain.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    Optional<Certificate> findByEnrollmentId(UUID enrollmentId);

    boolean existsByEnrollmentId(UUID enrollmentId);

    Optional<Certificate> findByValidationCode(String validationCode);
}