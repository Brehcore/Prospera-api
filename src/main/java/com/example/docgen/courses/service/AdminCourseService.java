package com.example.docgen.courses.service;

import com.example.docgen.courses.api.dto.CourseCreateRequest;
import com.example.docgen.courses.api.dto.LessonCreateRequest;
import com.example.docgen.courses.api.dto.ModuleCreateRequest;
import com.example.docgen.courses.domain.Course;
import com.example.docgen.courses.domain.Lesson;
import com.example.docgen.courses.domain.Module;
import com.example.docgen.courses.repositories.CourseRepository;
import com.example.docgen.courses.repositories.LessonRepository;
import com.example.docgen.courses.repositories.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminCourseService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public Course createCourse(CourseCreateRequest dto) {
        courseRepository.findByTitle(dto.title()).ifPresent(course -> {
            throw new IllegalStateException("Um curso com este título já existe.");
        });

        Course newCourse = Course.builder()
                .title(dto.title())
                .description(dto.description())
                .contentType(dto.contentType())
                .author(dto.author())
                .modules(new ArrayList<>())
                .build();
        return courseRepository.save(newCourse);
    }

    @Transactional
    public Module addModuleToCourse(UUID courseId, ModuleCreateRequest dto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Curso não encontrado com o ID: " + courseId));

        Module newModule = Module.builder()
                .course(course)
                .title(dto.title())
                .moduleOrder(dto.moduleOrder())
                .build();
        return moduleRepository.save(newModule);
    }

    @Transactional
    public Lesson addLessonToModule(UUID moduleId, LessonCreateRequest dto) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Módulo não encontrado com o ID: " + moduleId));

        Lesson newLesson = Lesson.builder()
                .module(module)
                .title(dto.title())
                .content(dto.content())
                .lessonOrder(dto.lessonOrder())
                .build();
        return lessonRepository.save(newLesson);
    }
}