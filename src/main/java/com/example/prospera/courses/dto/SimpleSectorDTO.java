package com.example.prospera.courses.dto;

import java.util.UUID;

// Este é o novo DTO que você precisa criar.
// A única finalidade dele é carregar o ID e o Nome de um setor para exibição.
public record SimpleSectorDTO(
        UUID id,
        String name
) {
}