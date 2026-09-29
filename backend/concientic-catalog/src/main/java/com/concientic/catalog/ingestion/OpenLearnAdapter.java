package com.concientic.catalog.ingestion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Component
public class OpenLearnAdapter implements SourceAdapter {
    private final List<URI> seedUrls;

    public OpenLearnAdapter(@Value("${concientic.ingestion.openlearn.urls:}") String configuredUrls) {
        this(parseUrls(configuredUrls));
    }

    OpenLearnAdapter(List<URI> seedUrls) {
        this.seedUrls = List.copyOf(seedUrls);
    }

    @Override
    public String sourceName() {
        return "OpenLearn";
    }

    @Override
    public List<ResourceCandidate> discover() {
        return seedUrls.stream()
                .map(url -> new ResourceCandidate(null, sourceName(), url, "OpenLearn", "university", null, List.of()))
                .toList();
    }

    private static List<URI> parseUrls(String configuredUrls) {
        if (configuredUrls == null || configuredUrls.isBlank()) {
            return List.of();
        }
        return Arrays.stream(configuredUrls.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(URI::create)
                .toList();
    }
}
