package com.concientic.catalog.api;

import java.time.Instant;
import java.util.UUID;

public record ResourceCheckResponse(
        UUID id,
        UUID ingestionRunId,
        String requestedUrl,
        String finalUrl,
        Integer httpStatus,
        String accessStatus,
        Boolean requiresAccount,
        Boolean requiresPayment,
        Instant checkedAt,
        String errorCode,
        String errorMessage) {
}
