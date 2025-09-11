package com.example.docgen.common.validation;

import br.com.caelum.stella.validation.CPFValidator;
import br.com.caelum.stella.validation.InvalidStateException;
import org.springframework.stereotype.Service;

@Service
public class CpfValidationService {

    private final CPFValidator cpfValidator;

    public CpfValidationService() {
        this.cpfValidator = new CPFValidator();
    }

    public boolean isValid(String cpf) {
        try {
            cpfValidator.assertValid(cpf);
            return true;
        } catch (InvalidStateException e) {
            return false;
        }
    }
}
