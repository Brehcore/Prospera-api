package com.example.docgen.courses.service;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.repositories.AuthUserRepository;
import com.example.docgen.courses.domain.Course;
import com.example.docgen.courses.domain.Enrollment;
import com.example.docgen.courses.domain.enums.EnrollmentStatus;
import com.example.docgen.courses.repositories.CourseRepository;
import com.example.docgen.courses.repositories.EnrollmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final AuthUserRepository authUserRepository;

    @Transactional
    public Enrollment enrollUserInCourse(AuthUser user, UUID courseId) {
        // A busca pelo curso deve ser feita apenas uma vez.
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Curso não encontrado com o ID: " + courseId));

        // VERIFICAÇÃO DE PERMISSÃO (o getOrganizationId() agora vai funcionar)
        if (course.getOrganizationId() != null && !course.getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Você não tem permissão para se matricular neste curso.");
        }

        // CORREÇÃO: A variável agora é do tipo correto 'Enrollment'
        Enrollment newEnrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .status(EnrollmentStatus.IN_PROGRESS)
                .progressPercentage(BigDecimal.ZERO)
                .build();

        return enrollmentRepository.save(newEnrollment);
    }
}