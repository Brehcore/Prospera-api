package com.example.prospera.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmEmailChangeRequest(
        @NotBlank String code
) {
}