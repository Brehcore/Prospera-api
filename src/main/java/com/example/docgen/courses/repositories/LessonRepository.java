package com.example.docgen.courses.repositories;

import com.example.docgen.courses.domain.Lesson;
import com.example.docgen.courses.domain.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    /**
     * CORREÇÃO: Conta o total de lições de um curso usando uma consulta explícita (JPQL).
     * Isso é mais confiável do que a derivação pelo nome do método.
     *
     * @param courseId O ID do curso para o qual as lições serão contadas.
     * @return O número total de lições no curso.
     */
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module.course.id = :courseId")
    long countByCourseId(@Param("courseId") UUID courseId);

    // Conta o total de lições de um curso de forma otimizada
    long countByModule_Course_Id(UUID courseId);

    Optional<Lesson> findByModuleAndLessonOrder(Module module, int lessonOrder);

}
