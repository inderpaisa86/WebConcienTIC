package com.concientic.catalog.api;

import java.time.Instant;

public record CatalogStatusResponse(
        String catalogVersion,
        Instant lastSuccessfulRun,
        long publishedResources,
        Instant lastVerifiedAt) {
}
