package com.example.docgen.courses.api;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.courses.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    // DTO para receber o progresso do Ebook
    public record EbookProgressRequest(int lastPageRead) {
    }

    @PutMapping("/ebooks/{trainingId}")
    public ResponseEntity<Void> updateEbookProgress(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID trainingId,
            @RequestBody EbookProgressRequest request) {
        progressService.updateEbookProgress(user.getId(), trainingId, request.lastPageRead());
        return ResponseEntity.ok().build();
    }
}