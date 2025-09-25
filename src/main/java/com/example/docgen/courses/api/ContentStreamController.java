package com.example.docgen.courses.api;

import com.example.docgen.courses.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stream")
@RequiredArgsConstructor
public class ContentStreamController {

    private final FileStorageService fileStorageService;

    // Você precisará de um serviço para buscar o EbookTraining e pegar o filePath
    // private final TrainingRepository trainingRepository;

    @GetMapping("/ebooks/{filename:.+}")
    public ResponseEntity<Resource> serveEbook(@PathVariable String filename) {
        // A lógica real buscaria o 'EbookTraining' pelo ID, pegaria o 'filename'
        // e o passaria para o fileStorageService.
        Resource file = fileStorageService.loadAsResource(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes") // Essencial para PDF.js
                .body(file);
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        Resource file = fileStorageService.loadAsResource(filename);
        // Este header é otimizado para imagens
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/jpeg").body(file);
    }
}