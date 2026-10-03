package com.concientic.catalog.ingestion;

import java.net.URI;
import java.time.Instant;

public record VerificationResult(
        AccessStatus accessStatus,
        URI requestedUrl,
        URI finalUrl,
        URI canonicalUrl,
        Integer httpStatus,
        long responseTimeMs,
        String title,
        String descriptionExcerpt,
        boolean semanticMatch,
        boolean requiresAccount,
        boolean requiresPayment,
        Instant checkedAt,
        String evidenceSummary,
        String errorCode,
        String errorMessage) {

    public boolean isPublishableAccess() {
        return (accessStatus == AccessStatus.ACTIVE || accessStatus == AccessStatus.LINK_CHANGED)
                && semanticMatch && !requiresPayment;
    }

    public boolean isAutomaticallyPublishable() {
        return isPublishableAccess()
                && title != null && !title.isBlank()
                && !requiresAccount;
    }
}
