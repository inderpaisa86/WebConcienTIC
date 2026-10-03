package com.concientic.catalog.api;

import java.time.Instant;
import java.util.UUID;

public record ReviewQueueItemResponse(
        UUID id,
        UUID resourceId,
        String title,
        String candidateUrl,
        String issue,
        String evidence,
        String agentDecision,
        Double confidenceScore,
        String recommendedAction,
        String status,
        Instant createdAt,
        Instant resolvedAt,
        String reviewerId,
        String humanDecision,
        String decisionNotes,
        String resourceStatus,
        String freeStatus,
        int version) {
}
