package com.example.docgen.courses.service.dto;

import java.util.UUID;

// Este record representa a resposta JSON do endpoint /admin/sectors/{id}
public record SectorDTO(UUID id, String name) {
}