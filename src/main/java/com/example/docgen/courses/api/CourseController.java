package com.example.docgen.courses.api;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.courses.api.dto.CourseSummaryDTO;
import com.example.docgen.courses.api.dto.EnrollmentResponseDTO;
import com.example.docgen.courses.service.CourseViewService;
import com.example.docgen.courses.service.EnrollmentService;
import com.example.docgen.courses.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/courses")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class CourseController {

    // Injetando os serviços corretos para cada responsabilidade
    private final CourseViewService courseViewService;
    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;

    /**
     * Endpoint para listar todos os cursos disponíveis no catálogo.
     * Utiliza o CourseViewService para buscar os cursos já com os dados enriquecidos (ex: nome do setor).
     */
    @GetMapping
    public ResponseEntity<List<CourseSummaryDTO>> listAvailableCourses(@AuthenticationPrincipal AuthUser user) {
        // Passe o objeto 'user' para o serviço
        List<CourseSummaryDTO> courses = courseViewService.findAllCourses(user);
        return ResponseEntity.ok(courses);
    }

    /**
     * Endpoint para um usuário se matricular em um curso.
     */
    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<EnrollmentResponseDTO> enrollInCourse(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID courseId) {

        var newEnrollment = enrollmentService.enrollUserInCourse(user, courseId);
        return ResponseEntity.ok(EnrollmentResponseDTO.fromEntity(newEnrollment));
    }

    /**
     * Endpoint para um usuário marcar uma lição como concluída.
     */
    @PostMapping("/lessons/{lessonId}/complete")
    @PreAuthorize("hasRole('USER')") // Garante que apenas usuários com essa role podem acessar
    public ResponseEntity<Void> markLessonAsCompleted(
            @AuthenticationPrincipal AuthUser user, // Injeta o usuário autenticado diretamente
            @PathVariable UUID lessonId) {

        // Usa o ID do usuário injetado para registrar o progresso
        progressService.markLessonAsCompleted(user.getId(), lessonId);

        return ResponseEntity.ok().build();
    }
}