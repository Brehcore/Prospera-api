package com.example.prospera.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SectorNotFoundException extends RuntimeException {
    public SectorNotFoundException(String message) {
        super(message);
    }
}