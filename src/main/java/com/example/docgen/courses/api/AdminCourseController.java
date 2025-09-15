package com.example.docgen.courses.api;

import com.example.docgen.courses.api.dto.CourseCreateRequest;
import com.example.docgen.courses.api.dto.LessonCreateRequest;
import com.example.docgen.courses.api.dto.ModuleCreateRequest;
import com.example.docgen.courses.domain.Course;
import com.example.docgen.courses.domain.Lesson;
import com.example.docgen.courses.domain.Module;
import com.example.docgen.courses.service.AdminCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/admin/courses")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class AdminCourseController {

    private final AdminCourseService adminCourseService;

    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody @Valid CourseCreateRequest dto) {
        Course newCourse = adminCourseService.createCourse(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(newCourse.getId()).toUri();
        return ResponseEntity.created(location).body(newCourse);
    }

    @PostMapping("/{courseId}/modules")
    public ResponseEntity<Module> addModule(
            @PathVariable UUID courseId,
            @RequestBody @Valid ModuleCreateRequest dto) {
        Module newModule = adminCourseService.addModuleToCourse(courseId, dto);
        // Pode-se criar uma URI para o novo módulo se houver um endpoint para buscá-lo
        return ResponseEntity.status(HttpStatus.CREATED).body(newModule);
    }

    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<Lesson> addLesson(
            @PathVariable UUID moduleId,
            @RequestBody @Valid LessonCreateRequest dto) {
        Lesson newLesson = adminCourseService.addLessonToModule(moduleId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newLesson);
    }
}