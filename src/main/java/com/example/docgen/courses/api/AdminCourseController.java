package com.example.docgen.courses.api;

import com.example.docgen.courses.api.dto.CourseCreateRequest;
import com.example.docgen.courses.api.dto.CourseDTO;
import com.example.docgen.courses.api.dto.CourseDetailDTO;
import com.example.docgen.courses.api.dto.CourseSummaryDTO;
import com.example.docgen.courses.api.dto.CourseUpdateRequest;
import com.example.docgen.courses.api.dto.LessonCreateRequest;
import com.example.docgen.courses.api.dto.LessonDTO;
import com.example.docgen.courses.api.dto.LessonUpdateRequest;
import com.example.docgen.courses.api.dto.ModuleCreateRequest;
import com.example.docgen.courses.api.dto.ModuleDTO;
import com.example.docgen.courses.api.dto.ModuleUpdateRequest;
import com.example.docgen.courses.domain.enums.PublicationStatus;
import com.example.docgen.courses.service.AdminCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/courses")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class AdminCourseController {

    private final AdminCourseService adminCourseService;

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@RequestBody @Valid CourseCreateRequest dto) {
        // CORREÇÃO: O serviço agora retorna um DTO
        CourseDTO newCourse = adminCourseService.createCourse(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(newCourse.id()).toUri();
        return ResponseEntity.created(location).body(newCourse);
    }

    @PostMapping("/{courseId}/modules")
    public ResponseEntity<ModuleDTO> addModule(
            @PathVariable UUID courseId,
            @RequestBody @Valid ModuleCreateRequest dto) {
        // CORREÇÃO: O serviço agora retorna um DTO
        ModuleDTO newModule = adminCourseService.addModuleToCourse(courseId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newModule);
    }

    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<LessonDTO> addLesson(
            @PathVariable UUID moduleId,
            @RequestBody @Valid LessonCreateRequest dto) {
        // CORREÇÃO: O serviço agora retorna um DTO
        LessonDTO newLesson = adminCourseService.addLessonToModule(moduleId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newLesson);
    }

    @GetMapping
    public ResponseEntity<List<CourseSummaryDTO>> getAllCourses() {
        // CORREÇÃO: O serviço agora é o responsável por montar a lista de DTOs
        List<CourseSummaryDTO> courses = adminCourseService.getAllCoursesAsSummary();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDetailDTO> getCourseDetails(@PathVariable UUID courseId) {
        // CORREÇÃO: O serviço retorna o DTO detalhado
        CourseDetailDTO course = adminCourseService.getCourseDetails(courseId);
        return ResponseEntity.ok(course);
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable UUID courseId, @RequestBody @Valid CourseUpdateRequest dto) {
        // CORREÇÃO: Retorna o DTO do curso atualizado
        CourseDTO updatedCourse = adminCourseService.updateCourse(courseId, dto);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID courseId) {
        adminCourseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/modules/{moduleId}")
    public ResponseEntity<ModuleDTO> updateModule(
            @PathVariable UUID moduleId,
            @RequestBody @Valid ModuleUpdateRequest dto) {
        // CORREÇÃO: Retorna o DTO do módulo atualizado
        ModuleDTO updatedModule = adminCourseService.updateModule(moduleId, dto);
        return ResponseEntity.ok(updatedModule);
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonDTO> updateLesson(
            @PathVariable UUID lessonId,
            @RequestBody @Valid LessonUpdateRequest dto) {
        // CORREÇÃO: Retorna o DTO da lição atualizada
        LessonDTO updatedLesson = adminCourseService.updateLesson(lessonId, dto);
        return ResponseEntity.ok(updatedLesson);
    }

    @PostMapping("/{courseId}/publish") // CORREÇÃO: URL ajustada
    public ResponseEntity<Void> publishCourse(@PathVariable UUID courseId) {
        // Assumindo que este método existe no seu serviço
        adminCourseService.changeCourseStatus(courseId, PublicationStatus.PUBLISHED);
        return ResponseEntity.ok().build();
    }
}