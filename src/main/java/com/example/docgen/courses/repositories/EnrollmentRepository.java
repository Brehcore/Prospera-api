package com.example.docgen.courses.repositories;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.courses.domain.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    /**
     * Verifica se já existe uma matrícula para um usuário específico em um curso específico.
     * Usado para evitar matrículas duplicadas.
     */
    boolean existsByUserAndCourse_Id(AuthUser user, UUID courseId);

    // NOVO MÉTODO: Encontra a matrícula de um usuário em um curso específico
    Optional<Enrollment> findByUserIdAndCourseId(UUID userId, UUID courseId);

}