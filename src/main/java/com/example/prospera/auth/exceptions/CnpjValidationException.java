package com.example.prospera.auth.exceptions;

public class CnpjValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CnpjValidationException(String message) {
        super(message);
    }
}
