package com.example.docgen.courses.repositories;

import com.example.docgen.courses.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    Optional<Course> findByTitle(String title);

}
