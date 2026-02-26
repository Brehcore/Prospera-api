package com.example.prospera.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Retorna um erro HTTP 409 CONFLICT, ideal para regras de negócio quebradas
@ResponseStatus(HttpStatus.CONFLICT)
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
