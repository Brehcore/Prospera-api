package com.example.prospera.integration.service;

import com.example.prospera.integration.dto.BrasilApiCnpjResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CnpjLookupService {

    private final RestTemplate restTemplate = new RestTemplate();

    // A URL agora inclui o placeholder {cnpj} no final.
    private static final String BRASIL_API_URL = "https://brasilapi.com.br/api/cnpj/v1/{cnpj}";

    public BrasilApiCnpjResponse consultCnpj(String cnpj) {
        String cleanedCnpj = cnpj.replaceAll("[^0-9]", "");

        try {
            // A chamada continua a mesma, mas agora ela funciona porque a URL tem o placeholder.
            // O RestTemplate irá substituir {cnpj} pelo valor de cleanedCnpj.
            return restTemplate.getForObject(BRASIL_API_URL, BrasilApiCnpjResponse.class, cleanedCnpj);

        } catch (HttpClientErrorException.NotFound e) {
            // Seu tratamento de erro para CNPJ não encontrado está perfeito.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "O CNPJ informado não foi encontrado na base de dados da Receita Federal.");
        } catch (Exception e) {
            // Seu tratamento de erro genérico também está ótimo.
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível consultar o CNPJ neste momento. O serviço externo pode estar indisponível.");
        }
    }
}