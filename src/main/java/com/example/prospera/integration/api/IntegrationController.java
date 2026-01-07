package com.example.prospera.integration.api;

import com.example.prospera.integration.dto.BrasilApiCnpjResponse;
import com.example.prospera.integration.service.CnpjLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST responsável por expor endpoints de integração com serviços externos.
 * Fornece funcionalidades para consulta de dados cadastrais e empresariais.
 */
@RestController
@RequestMapping("/api/lookup")
@RequiredArgsConstructor
public class IntegrationController {


    private final CnpjLookupService cnpjLookupService;

    /**
     * Endpoint para consulta de dados cadastrais de empresas através do CNPJ.
     * Realiza integração com a API Brasil para obter informações detalhadas do CNPJ informado.
     *
     * @param cnpj número do CNPJ a ser consultado (apenas números)
     * @return ResponseEntity contendo os dados cadastrais da empresa
     */
    @GetMapping("/cnpj/{cnpj}")
    public ResponseEntity<BrasilApiCnpjResponse> getCnpjData(@PathVariable String cnpj) {
        BrasilApiCnpjResponse response = cnpjLookupService.consultCnpj(cnpj);
        return ResponseEntity.ok(response);
    }
}