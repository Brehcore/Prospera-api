package com.example.prospera.courses.repositories;

import com.example.prospera.courses.domain.Lesson;
import com.example.prospera.courses.domain.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module.course.id = :courseId")
    long countByCourseId(@Param("courseId") UUID courseId);

    long countByModule_Course_Id(UUID courseId);

    Optional<Lesson> findByModuleAndLessonOrder(Module module, int lessonOrder);


}
