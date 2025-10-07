package com.example.docgen.subscription.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubscriptionCreateRequest(

        @NotNull UUID userId,
        @NotNull UUID planId
) {
}
