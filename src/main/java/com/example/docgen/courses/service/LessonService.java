package com.example.docgen.courses.service;

import com.example.docgen.auth.exceptions.ResourceNotFoundException;
import com.example.docgen.courses.domain.Lesson;
import com.example.docgen.courses.domain.Module;
import com.example.docgen.courses.repositories.LessonRepository;
import com.example.docgen.courses.repositories.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;

    @Transactional(readOnly = true)
    public Optional<Lesson> findNextLesson(UUID currentLessonId) {
        Lesson currentLesson = lessonRepository.findById(currentLessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Aula com ID " + currentLessonId + " não encontrada."));

        Module currentModule = currentLesson.getModule();

        Optional<Lesson> nextLessonInModule = lessonRepository.findByModuleAndLessonOrder(currentModule, currentLesson.getLessonOrder() + 1);

        if (nextLessonInModule.isPresent()) {
            return nextLessonInModule;
        }

        Optional<Module> nextModule = moduleRepository.findByCourseAndModuleOrder(
                currentModule.getCourse(),
                currentModule.getModuleOrder() + 1
        );

        return nextModule.flatMap(module -> module.getLessons().stream().findFirst());
    }

    @Transactional(readOnly = true)
    public Optional<Lesson> findPreviousLesson(UUID currentLessonId) {
        Lesson currentLesson = lessonRepository.findById(currentLessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Aula com ID " + currentLessonId + " não encontrada."));

        if (currentLesson.getLessonOrder() > 1) {
            return lessonRepository.findByModuleAndLessonOrder(currentLesson.getModule(), currentLesson.getLessonOrder() - 1);
        }

        Module currentModule = currentLesson.getModule();
        Optional<Module> previousModule = moduleRepository.findByCourseAndModuleOrder(
                currentModule.getCourse(),
                currentModule.getModuleOrder() - 1
        );

        return previousModule.flatMap(module -> {
            List<Lesson> lessons = module.getLessons();
            if (lessons.isEmpty()) {
                // Se o módulo não tiver aulas, não há aula anterior para retornar.
                return Optional.empty();
            }
            return Optional.of(lessons.getLast());
        });
    }
}