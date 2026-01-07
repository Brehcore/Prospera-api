package com.example.prospera.courses.api.dto;

import com.example.prospera.courses.domain.Module;

import java.util.UUID;

public record ModuleDTO(UUID id, String title, int order) {
    public static ModuleDTO fromEntity(Module module) {
        return new ModuleDTO(module.getId(), module.getTitle(), module.getModuleOrder());
    }
}