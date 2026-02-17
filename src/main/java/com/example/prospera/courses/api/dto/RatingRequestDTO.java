package com.example.prospera.courses.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RatingRequestDTO(
        @NotNull @Min(1) @Max(5) Integer score,
        String comment
) {
}