package com.example.prospera.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // Essencial para ignorar outros campos
public class BrasilApiCnpjResponse {

    @JsonProperty("cnpj")
    private String cnpj;

    @JsonProperty("razao_social")
    private String razaoSocial;
}