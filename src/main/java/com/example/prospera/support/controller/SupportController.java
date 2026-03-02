package com.example.prospera.support.controller;

import com.example.prospera.support.dto.SupportTicketRequest;
import com.example.prospera.support.service.SupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;

    @PostMapping("/tickets")
    public ResponseEntity<Void> openTicket(@RequestBody @Valid SupportTicketRequest request) {
        supportService.processTicket(request);
        return ResponseEntity.ok().build();
    }
}