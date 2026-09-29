package com.concientic.catalog.ingestion;

import java.net.URI;

public record ResourceSnapshot(
        String title,
        URI canonicalUrl,
        AccessStatus accessStatus,
        String language,
        String duration,
        String format,
        String freeStatus) {
}
