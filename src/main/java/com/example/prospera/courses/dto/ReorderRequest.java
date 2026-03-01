package com.example.prospera.courses.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ReorderRequest(
        @NotNull(message = "A lista de itens não pode ser nula")
        List<OrderItem> items
) {
    public record OrderItem(
            @NotNull UUID id,
            @NotNull Integer newOrder
    ) {
    }
}