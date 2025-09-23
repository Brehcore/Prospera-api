package com.example.docgen.integration.api;

import com.example.docgen.integration.dto.BrasilApiCnpjResponse;
import com.example.docgen.integration.service.CnpjLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lookup")
@RequiredArgsConstructor
public class IntegrationController {

    private final CnpjLookupService cnpjLookupService;

    @GetMapping("/cnpj/{cnpj}")
    public ResponseEntity<BrasilApiCnpjResponse> getCnpjData(@PathVariable String cnpj) {
        BrasilApiCnpjResponse response = cnpjLookupService.consultCnpj(cnpj);
        return ResponseEntity.ok(response);
    }
}