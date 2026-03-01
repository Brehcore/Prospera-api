package com.example.prospera.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @NotBlank String token,
        @NotBlank String newPassword
) {
}