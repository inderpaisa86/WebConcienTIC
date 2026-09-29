package com.concientic.catalog.ingestion;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PipelineStagesTest {

    @Test
    void keepsTheDocumentedPipelineOrder() {
        assertThat(PipelineStages.ORDER).containsExactly(
                "DISCOVER", "FETCH", "EXTRACT", "VERIFY_SOURCE", "VERIFY_URL",
                "VERIFY_FREE_ACCESS", "CLASSIFY", "MAP_GUARDIAN", "QUALITY_CHECK",
                "DEDUPLICATE", "COMPARE", "CURATE", "UPDATE_DATA", "GENERATE_REPORT");
    }
}
