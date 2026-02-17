package com.example.prospera.common.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TrainingRatedEvent(
        UUID trainingId,
        UUID userId,
        Integer score,
        String comment,
        OffsetDateTime ratedAt
) {
}
