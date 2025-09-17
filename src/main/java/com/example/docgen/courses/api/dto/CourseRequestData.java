package com.example.docgen.courses.api.dto;

import com.example.docgen.courses.domain.enums.ContentType;
import com.example.docgen.courses.domain.enums.CourseModality;
import com.example.docgen.courses.domain.enums.PublicationStatus;
import com.example.docgen.courses.domain.enums.TrainingType;

import java.util.UUID;

// Definindo o "contrato" dos dados que um curso precisa para ser criado ou atualizado.
public interface CourseRequestData {
    String title();

    String description();

    ContentType contentType();

    String author();

    UUID setorId();

    CourseModality modality();

    PublicationStatus publicationStatus();

    TrainingType trainingType();
}