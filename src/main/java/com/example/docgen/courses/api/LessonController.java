package com.example.docgen.courses.api;

import com.example.docgen.courses.api.dto.LessonDTO;
import com.example.docgen.courses.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class LessonController {

    private final LessonService lessonService;

    @GetMapping("/{lessonId}/next")
    public ResponseEntity<LessonDTO> getNextLesson(@PathVariable UUID lessonId) {
        return lessonService.findNextLesson(lessonId)
                // Reutilizando seu método estático fromEntity para mapear
                .map(lesson -> ResponseEntity.ok(LessonDTO.fromEntity(lesson)))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{lessonId}/previous")
    public ResponseEntity<LessonDTO> getPreviousLesson(@PathVariable UUID lessonId) {
        return lessonService.findPreviousLesson(lessonId)
                .map(lesson -> ResponseEntity.ok(LessonDTO.fromEntity(lesson)))
                .orElse(ResponseEntity.noContent().build());
    }
}