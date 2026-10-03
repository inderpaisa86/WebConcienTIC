package com.concientic.catalog.api;

public record ReviewDecisionRequest(
        String reviewerId,
        String notes,
        String title,
        String description,
        String freeStatus,
        String resourceStatus) {
}
