package com.example.prospera.common.validation;

import br.com.caelum.stella.validation.CPFValidator;
import br.com.caelum.stella.validation.InvalidStateException;
import com.example.prospera.exceptions.CpfValidationException;
import org.springframework.stereotype.Service;

@Service
public class CpfValidationService {

    private final CPFValidator cpfValidator;

    public CpfValidationService() {
        this.cpfValidator = new CPFValidator();
    }

    /**
     * Valida o CPF. Se for inválido, lança CpfValidationException.
     *
     * @param cpf O CPF a ser validado.
     */
    public void validate(String cpf) { // Mude o nome e o retorno para void
        try {
            cpfValidator.assertValid(cpf);
        } catch (InvalidStateException e) {
            // Lança sua exceção customizada com uma mensagem clara
            throw new CpfValidationException("CPF inválido.");
        }
    }
}