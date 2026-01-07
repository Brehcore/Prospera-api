package com.example.prospera.subscription.dto;

import com.example.prospera.subscription.enums.SubscriptionOrigin;
import com.example.prospera.subscription.enums.SubscriptionStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SubscriptionResponse(

        UUID id,
        UUID planId,
        String planName, // Muito útil para exibir na tela sem outra consulta
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        SubscriptionStatus status,
        SubscriptionOrigin origin,

        // Dono da assinatura
        UUID ownerAccountId,
        String ownerName,
        UUID ownerUserId
) {
}
