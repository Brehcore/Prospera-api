package com.example.prospera.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @NotBlank String token,
        @NotBlank String newPassword
) {
}