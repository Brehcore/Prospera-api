package com.example.prospera.admin.dto;

import com.example.prospera.common.enums.UserRole;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AdminUserUpdateRequest(
        UserRole role,

        @Size(max = 255, message = "O nome deve ter no máximo 255 caracteres")
        String fullName,

        String cpf,
        LocalDate birthDate,
        String phone,
        String email
) {
}
