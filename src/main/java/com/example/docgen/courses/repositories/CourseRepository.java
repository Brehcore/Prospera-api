package com.example.docgen.courses.repositories;

import com.example.docgen.courses.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    /**
     * Encontra todos os cursos disponíveis para uma organização.
     * Inclui cursos públicos (sem organizationId) e os cursos da própria organização.
     */
    @Query("SELECT c FROM Course c WHERE (c.organizationId IS NULL OR c.organizationId = :orgId) AND c.status = 'PUBLISHED'")
    List<Course> findAvailableForOrganization(@Param("orgId") UUID orgId);

    Optional<Course> findByTitle(String title);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.modules m LEFT JOIN FETCH m.lessons WHERE c.id = :id")
    Optional<Course> findByIdWithModulesAndLessons(@Param("id") UUID id);

}
