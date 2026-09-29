package com.concientic.catalog.ingestion;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class ChangeMonitorTest {

    @Test
    void recordsUrlStatusAndPriceChangesWithoutReplacingHistory() {
        var previous = new ResourceSnapshot("Old title", URI.create("https://example.org/old"), AccessStatus.ACTIVE, "en", "2 h", "course", "FREE");
        var current = new ResourceSnapshot("New title", URI.create("https://example.org/new"), AccessStatus.LINK_CHANGED, "en", "3 h", "course", "PAID");

        var changes = ChangeMonitor.compare(previous, current);

        assertThat(changes).extracting(ResourceChange::changeType)
                .contains("UPDATED", "LINK_CHANGED", "STATUS_CHANGED", "DURATION_CHANGED", "PRICE_CHANGED");
        assertThat(changes).anyMatch(change -> change.fieldName().equals("canonical_url") && change.previousValue().contains("/old"));
    }
}
