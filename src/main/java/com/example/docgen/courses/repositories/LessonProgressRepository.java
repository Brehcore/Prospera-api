package com.example.docgen.courses.repositories;

import com.example.docgen.courses.domain.Enrollment;
import com.example.docgen.courses.domain.Lesson;
import com.example.docgen.courses.domain.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {

    // Verifica se um progresso para uma dada matrícula e lição já existe
    boolean existsByEnrollmentAndLesson(Enrollment enrollment, Lesson lesson);

    // Conta quantos progressos uma matrícula possui
    long countByEnrollment(Enrollment enrollment);

}