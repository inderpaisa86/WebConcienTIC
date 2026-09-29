package com.concientic.catalog.ingestion;

public record ResourceChange(
        String fieldName,
        String previousValue,
        String currentValue,
        String changeType) {
}
