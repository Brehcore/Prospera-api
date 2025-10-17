package com.example.docgen.subscription.dto;

import com.example.docgen.subscription.enums.AccessType;

import java.time.OffsetDateTime;

public record AccessStatusDTO(
        AccessType acessType,
        String planName,
        OffsetDateTime endDate,
        String organizationName //Será preenchido se o acesso for via organização
) {
}
