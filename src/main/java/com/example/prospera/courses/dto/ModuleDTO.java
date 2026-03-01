package com.example.prospera.courses.dto;

import com.example.prospera.courses.domain.Module;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record ModuleDTO(UUID id, String title, int order, List<LessonDTO> lessons) {
    public static ModuleDTO fromEntity(Module module) {
        return new ModuleDTO(module.getId(), module.getTitle(), module.getModuleOrder(), Collections.emptyList());
    }
}