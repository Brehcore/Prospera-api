package com.example.docgen.auth.admin;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminResetPasswordRequest(
        @NotBlank @Email
        String email,
        @NotBlank @Size(min = 8)
        String newPassword) {
}
