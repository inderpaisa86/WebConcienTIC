package com.concientic.catalog.domain;

import java.time.Instant;

public record Evidence(
        String kind,
        String url,
        Instant observedAt,
        String summary,
        String hash) {
}
