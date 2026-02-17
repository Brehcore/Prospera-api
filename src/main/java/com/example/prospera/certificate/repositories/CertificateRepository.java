package com.example.prospera.certificate.repositories;

import com.example.prospera.certificate.domain.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    Optional<Certificate> findByEnrollmentId(UUID enrollmentId);

    boolean existsByEnrollmentId(UUID enrollmentId);

    Optional<Certificate> findByValidationCode(String validationCode);

    @Query("""
               SELECT c FROM Certificate c
               JOIN c.enrollment e
               JOIN e.training t
               WHERE e.user.id = :userId
               AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')))
               ORDER BY c.issuedAt DESC
            """)
    List<Certificate> findMyCertificates(
            @Param("userId") UUID userId,
            @Param("search") String search
    );
}