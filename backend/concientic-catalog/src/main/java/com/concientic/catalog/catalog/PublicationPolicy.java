package com.concientic.catalog.catalog;

import com.concientic.catalog.domain.FreeStatus;
import com.concientic.catalog.domain.ResourceStatus;

import java.util.Set;

public final class PublicationPolicy {
    private static final Set<FreeStatus> PUBLISHABLE_FREE_STATUSES = Set.of(
            FreeStatus.FREE,
            FreeStatus.FREE_CONTENT_PAID_CERTIFICATE,
            FreeStatus.AUDIT_FREE,
            FreeStatus.PARTIAL_FREE);

    private static final Set<ResourceStatus> PUBLISHABLE_RESOURCE_STATUSES = Set.of(
            ResourceStatus.ACTIVE,
            ResourceStatus.UPDATED,
            ResourceStatus.VERIFIED,
            ResourceStatus.LINK_CHANGED);

    private PublicationPolicy() {
    }

    public static Decision evaluate(ResourceStatus status, FreeStatus freeStatus, boolean hasEvidence) {
        if (!hasEvidence) {
            return new Decision(false, "REVIEW_REQUIRED: falta evidencia verificable");
        }
        if (!PUBLISHABLE_RESOURCE_STATUSES.contains(status)) {
            return new Decision(false, "REVIEW_REQUIRED: estado no publicable");
        }
        if (!PUBLISHABLE_FREE_STATUSES.contains(freeStatus)) {
            return new Decision(false, "PAYMENT_REQUIRED_OR_UNKNOWN: modalidad no publicable automáticamente");
        }
        return new Decision(true, "PUBLICABLE: cumple estado, gratuidad y evidencia mínima");
    }

    public record Decision(boolean publishable, String explanation) {
    }
}
