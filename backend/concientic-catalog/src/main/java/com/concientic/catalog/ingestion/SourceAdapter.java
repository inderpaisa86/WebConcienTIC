package com.concientic.catalog.ingestion;

import java.util.List;

public interface SourceAdapter {
    String sourceName();

    List<ResourceCandidate> discover();
}
