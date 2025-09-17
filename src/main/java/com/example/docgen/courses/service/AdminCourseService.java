package com.example.docgen.courses.service;

import com.example.docgen.courses.api.dto.CourseCreateRequest;
import com.example.docgen.courses.api.dto.CourseDTO;
import com.example.docgen.courses.api.dto.CourseDetailDTO;
import com.example.docgen.courses.api.dto.CourseRequestData;
import com.example.docgen.courses.api.dto.CourseSummaryDTO;
import com.example.docgen.courses.api.dto.CourseUpdateRequest;
import com.example.docgen.courses.api.dto.LessonCreateRequest;
import com.example.docgen.courses.api.dto.LessonDTO;
import com.example.docgen.courses.api.dto.LessonUpdateRequest;
import com.example.docgen.courses.api.dto.ModuleCreateRequest;
import com.example.docgen.courses.api.dto.ModuleDTO;
import com.example.docgen.courses.api.dto.ModuleUpdateRequest;
import com.example.docgen.courses.domain.Course;
import com.example.docgen.courses.domain.Lesson;
import com.example.docgen.courses.domain.Module;
import com.example.docgen.courses.domain.enums.PublicationStatus;
import com.example.docgen.courses.repositories.CourseRepository;
import com.example.docgen.courses.repositories.LessonRepository;
import com.example.docgen.courses.repositories.ModuleRepository;
import com.example.docgen.courses.service.dto.SectorDTO;
import com.example.docgen.courses.service.exception.SectorNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCourseService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final WebClient enterpriseWebClient;

    // --- MÉTODOS CORRIGIDOS PARA RETORNAR DTOs ---

    @Transactional
    public CourseDTO createCourse(CourseCreateRequest request) { // CORREÇÃO: Retorna CourseDTO
        validateSectorExists(request.setorId()).block();
        Course newCourse = new Course();
        populateCourseFromRequest(newCourse, request);
        Course savedCourse = courseRepository.save(newCourse);
        return CourseDTO.fromEntity(savedCourse);
    }

    @Transactional
    public CourseDTO updateCourse(UUID courseId, CourseUpdateRequest request) { // CORREÇÃO: Retorna CourseDTO
        validateSectorExists(request.setorId()).block();
        Course course = getCourseById(courseId);
        populateCourseFromRequest(course, request);
        Course updatedCourse = courseRepository.save(course);
        return CourseDTO.fromEntity(updatedCourse);
    }

    @Transactional
    public ModuleDTO addModuleToCourse(UUID courseId, ModuleCreateRequest dto) { // CORREÇÃO: Retorna ModuleDTO
        Course course = getCourseById(courseId);
        Module newModule = Module.builder()
                .course(course)
                .title(dto.title())
                .moduleOrder(dto.moduleOrder())
                .build();
        Module savedModule = moduleRepository.save(newModule);
        return ModuleDTO.fromEntity(savedModule);
    }

    @Transactional
    public LessonDTO addLessonToModule(UUID moduleId, LessonCreateRequest dto) { // CORREÇÃO: Retorna LessonDTO
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Módulo não encontrado com o ID: " + moduleId));
        Lesson newLesson = Lesson.builder()
                .module(module)
                .title(dto.title())
                .content(dto.content())
                .lessonOrder(dto.lessonOrder())
                .build();
        Lesson savedLesson = lessonRepository.save(newLesson);
        return LessonDTO.fromEntity(savedLesson);
    }

    @Transactional
    public ModuleDTO updateModule(UUID moduleId, ModuleUpdateRequest dto) { // CORREÇÃO: Retorna ModuleDTO
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Módulo não encontrado com o ID: " + moduleId));
        module.setTitle(dto.title());
        module.setModuleOrder(dto.moduleOrder());
        Module updatedModule = moduleRepository.save(module);
        return ModuleDTO.fromEntity(updatedModule);
    }

    @Transactional
    public LessonDTO updateLesson(UUID lessonId, LessonUpdateRequest dto) { // CORREÇÃO: Retorna LessonDTO
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lição não encontrada com o ID: " + lessonId));
        lesson.setTitle(dto.title());
        lesson.setContent(dto.content());
        lesson.setLessonOrder(dto.lessonOrder());
        Lesson updatedLesson = lessonRepository.save(lesson);
        return LessonDTO.fromEntity(updatedLesson);
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryDTO> getAllCoursesAsSummary() { // CORREÇÃO: Novo método para o controller
        List<Course> courses = courseRepository.findAll();
        return courses.stream()
                .map(course -> {
                    String sectorName = getSectorName(course.getSectorId());
                    return CourseSummaryDTO.fromEntity(course, sectorName);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDetailDTO getCourseDetails(UUID courseId) { // CORREÇÃO: Retorna CourseDetailDTO
        Course course = courseRepository.findByIdWithModulesAndLessons(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Curso não encontrado: " + courseId));
        return CourseDetailDTO.fromEntity(course);
    }

    // --- MÉTODO FALTANTE ADICIONADO ---

    @Transactional
    public void changeCourseStatus(UUID courseId, PublicationStatus status) {
        Course course = getCourseById(courseId);
        course.setStatus(status);
        courseRepository.save(course);
    }

    // --- MÉTODOS PRIVADOS E DE DELEÇÃO (JÁ ESTAVAM OK) ---

    @Transactional
    public void deleteCourse(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new EntityNotFoundException("Curso não encontrado: " + courseId);
        }
        courseRepository.deleteById(courseId);
    }

    // ... (outros métodos de delete que você já tinha) ...
    @Transactional
    public void deleteModule(UUID moduleId) {
        if (!moduleRepository.existsById(moduleId)) {
            throw new EntityNotFoundException("Módulo não encontrado: " + moduleId);
        }
        moduleRepository.deleteById(moduleId);
    }

    @Transactional
    public void deleteLesson(UUID lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new EntityNotFoundException("Lição não encontrada: " + lessonId);
        }
        lessonRepository.deleteById(lessonId);
    }

    public Course getCourseById(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Curso não encontrado com o ID: " + courseId));
    }

    private void populateCourseFromRequest(Course course, CourseRequestData request) {
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setContentType(request.contentType());
        course.setAuthor(request.author());
        course.setSectorId(request.setorId());
        course.setModality(request.modality());
        course.setStatus(request.publicationStatus());
        course.setTrainingType(request.trainingType());
    }

    private Mono<Void> validateSectorExists(UUID sectorId) {
        return enterpriseWebClient.get()
                .uri("/admin/sectors/{id}", sectorId)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        clientResponse -> Mono.error(new SectorNotFoundException("Setor com ID " + sectorId + " não encontrado.")))
                .bodyToMono(Void.class);
    }

    private String getSectorName(UUID sectorId) {
        SectorDTO sector = enterpriseWebClient.get()
                .uri("/admin/sectors/{id}", sectorId)
                .retrieve()
                .bodyToMono(SectorDTO.class)
                .onErrorReturn(new SectorDTO(sectorId, "Setor não encontrado"))
                .block();

        return Optional.ofNullable(sector)
                .map(SectorDTO::name)
                .orElse("Setor inválido ou não encontrado");
    }
}