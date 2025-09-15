package com.example.docgen.courses.repositories;

import com.example.docgen.courses.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    // Método essencial para o futuro: buscar todas as lições de um módulo específico,
    // já ordenadas pela coluna 'lessonOrder'.
    List<Lesson> findByModule_IdOrderByLessonOrderAsc(UUID moduleId);
}
