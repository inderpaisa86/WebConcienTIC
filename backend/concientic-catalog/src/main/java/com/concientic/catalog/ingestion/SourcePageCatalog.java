package com.concientic.catalog.ingestion;

import java.util.List;

public record SourcePageCatalog(
        String version,
        String policy,
        List<ConfiguredSourcePage> pages) {

    public SourcePageCatalog {
        pages = pages == null ? List.of() : List.copyOf(pages);
    }
}
