package com.example.prospera.courses.repositories;

import com.example.prospera.courses.domain.Module;
import com.example.prospera.courses.domain.RecordedCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID> {

    // Método essencial para o futuro: buscar todos os módulos de um curso específico,
    // já ordenados pela coluna 'moduleOrder'.
    List<Module> findByCourse_IdOrderByModuleOrderAsc(UUID courseId);

    /**
     * Verifica se existe algum módulo para um curso (treinamento) específico.
     * Nota: O nome 'Course' deve corresponder ao nome do campo na sua entidade Module.
     */
    boolean existsByCourseId(UUID courseId);

    Optional<Module> findByCourseAndModuleOrder(RecordedCourse course, int moduleOrder);

    List<Module> findAllByCourse_IdOrderByModuleOrder(UUID trainingId);

    // COALESCE garante que retorne 0 se não houver aulas, evitando NullPointerException
    @Query("SELECT COALESCE(SUM(l.durationInMinutes), 0) FROM Module m JOIN m.lessons l WHERE m.course.id = :trainingId")
    Integer calculateTotalDurationByTrainingId(@Param("trainingId") UUID trainingId);
}
