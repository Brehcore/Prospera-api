package com.example.docgen.common.validation;

import br.com.caelum.stella.validation.CNPJValidator;
import org.springframework.stereotype.Service;

@Service
public class CnpjValidationService {

    private final CNPJValidator cnpjValidator;

    public CnpjValidationService() {
        this.cnpjValidator = new CNPJValidator();
    }

    public boolean isValid(String cnpj) {
        try {
            cnpjValidator.assertValid(cnpj);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
