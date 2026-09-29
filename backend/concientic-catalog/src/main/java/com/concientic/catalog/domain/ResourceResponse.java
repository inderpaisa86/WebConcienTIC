package com.concientic.catalog.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ResourceResponse(
        UUID id,
        String title,
        String shortDescription,
        String provider,
        String providerType,
        String country,
        String sourceUrl,
        String canonicalUrl,
        String verifiedUrl,
        String urlStatus,
        Integer httpStatus,
        Instant lastVerifiedAt,
        Instant firstDiscoveredAt,
        Instant lastUpdatedAt,
        FreeStatus freeStatus,
        String freeExplanation,
        List<String> language,
        String level,
        String duration,
        String format,
        String certificate,
        String primaryCompetency,
        List<String> secondaryCompetencies,
        String guardianPrimary,
        List<String> guardianSecondary,
        List<String> topics,
        List<String> audience,
        String trustLevel,
        Double trustScore,
        Double relevanceScore,
        Double qualityScore,
        Double freshnessScore,
        Double verificationScore,
        Double overallScore,
        List<Evidence> evidence,
        String reasonForInclusion,
        ResourceStatus status,
        UUID duplicateOf,
        Instant sourceLastChecked,
        String notes) {
}
