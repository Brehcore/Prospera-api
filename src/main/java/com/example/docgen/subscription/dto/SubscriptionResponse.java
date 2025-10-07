package com.example.docgen.subscription.dto;

import com.example.docgen.subscription.enums.SubscriptionOrigin;
import com.example.docgen.subscription.enums.SubscriptionStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SubscriptionResponse(

        UUID id,
        UUID userId,
        UUID planId,
        String planName, // Muito útil para exibir na tela sem outra consulta
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        SubscriptionStatus status,
        SubscriptionOrigin origin
) {
}
