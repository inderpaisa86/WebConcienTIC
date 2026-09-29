package com.concientic.catalog.ingestion;

import java.util.List;

public final class PipelineStages {
    public static final List<String> ORDER = List.of(
            "DISCOVER", "FETCH", "EXTRACT", "VERIFY_SOURCE", "VERIFY_URL",
            "VERIFY_FREE_ACCESS", "CLASSIFY", "MAP_GUARDIAN", "QUALITY_CHECK",
            "DEDUPLICATE", "COMPARE", "CURATE", "UPDATE_DATA", "GENERATE_REPORT");

    private PipelineStages() {
    }
}
