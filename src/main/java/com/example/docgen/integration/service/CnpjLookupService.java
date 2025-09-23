package com.example.docgen.integration.service;

import com.example.docgen.integration.dto.BrasilApiCnpjResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CnpjLookupService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BRASIL_API_URL = "https://brasilapi.com.br/api/cnpj/v1/"; //

    public BrasilApiCnpjResponse consultCnpj(String cnpj) {
        String cleanedCnpj = cnpj.replaceAll("[^0-9]", "");
        String url = BRASIL_API_URL + cleanedCnpj;

        try {
            return restTemplate.getForObject(url, BrasilApiCnpjResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            // Erro 404: A BrasilAPI informou que o CNPJ não foi encontrado.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "O CNPJ informado não foi encontrado na base de dados da Receita Federal.");
        } catch (Exception e) {
            // Outros erros: Falha de rede, API fora do ar, etc.
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível consultar o CNPJ neste momento. O serviço externo pode estar indisponível.");
        }
    }
}