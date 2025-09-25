package com.example.docgen.courses.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("EBOOK") // Identifica esta classe na tabela 'trainings'
public class EbookTraining extends Training {

    private String filePath; // Caminho/chave para o arquivo PDF no storage

    private Integer totalPages; // Total de páginas, útil para a barra de progresso

    private OffsetDateTime fileUploadedAt;

}