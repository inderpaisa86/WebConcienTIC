package com.concientic.catalog.ingestion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ConfiguredSourcePageAdapter implements SourceAdapter {
    private static final Logger log = LoggerFactory.getLogger(ConfiguredSourcePageAdapter.class);

    private final List<ConfiguredSourcePage> pages;

    @Autowired
    public ConfiguredSourcePageAdapter(
            ResourceLoader resourceLoader,
            @Value("${concientic.ingestion.source-pages:classpath:source-pages.json}") String location) {
        this.pages = loadPages(resourceLoader, location);
    }

    ConfiguredSourcePageAdapter(List<ConfiguredSourcePage> pages) {
        this.pages = pages == null ? List.of() : List.copyOf(pages);
    }

    @Override
    public String sourceName() {
        return "ConfiguredSourcePages";
    }

    @Override
    public List<ResourceCandidate> discover() {
        return pages.stream()
                .map(this::toCandidateSafely)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private ResourceCandidate toCandidateSafely(ConfiguredSourcePage page) {
        try {
            return page.toCandidate();
        } catch (RuntimeException exception) {
            log.warn("Configured source page ignored source={} url={} message={}",
                    page.sourceName(), page.url(), exception.getMessage());
            return null;
        }
    }

    private static List<ConfiguredSourcePage> loadPages(ResourceLoader resourceLoader, String location) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                throw new IllegalStateException("Source pages registry does not exist: " + location);
            }
            SourcePageCatalog catalog = objectMapper.readValue(resource.getInputStream(), SourcePageCatalog.class);
            return catalog.pages();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load source pages registry: " + location, exception);
        }
    }
}
