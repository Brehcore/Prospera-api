package com.example.prospera.subscription.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubscriptionCreateRequest(

        @NotNull UUID userId,
        @NotNull UUID planId
) {
}
