package com.example.prospera.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InitiateEmailChangeRequest(
        @NotBlank @Email String currentEmail,
        @NotBlank @Email String newEmail
) {
}
