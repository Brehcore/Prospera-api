package com.example.prospera.common.validation;

import br.com.caelum.stella.validation.CNPJValidator;
import com.example.prospera.auth.exceptions.CnpjValidationException;
import org.springframework.stereotype.Service;

@Service
public class CnpjValidationService {

    private final CNPJValidator cnpjValidator;

    public CnpjValidationService() {
        this.cnpjValidator = new CNPJValidator();
    }

    /**
     * Valida o CNPJ. Se for inválido, lança CnpjValidationException.
     *
     * @param cnpj O CNPJ a ser validado.
     */
    public void validate(String cnpj) { // Mude o nome e o retorno para void
        try {
            cnpjValidator.assertValid(cnpj);
        } catch (Exception e) {
            // Lança sua exceção customizada
            throw new CnpjValidationException("CNPJ inválido.");
        }
    }
}