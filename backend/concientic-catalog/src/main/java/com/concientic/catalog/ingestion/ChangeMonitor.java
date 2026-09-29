package com.concientic.catalog.ingestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ChangeMonitor {
    private ChangeMonitor() {
    }

    public static List<ResourceChange> compare(ResourceSnapshot previous, ResourceSnapshot current) {
        if (previous == null || current == null) return List.of();
        List<ResourceChange> changes = new ArrayList<>();
        add(changes, "title", previous.title(), current.title(), "UPDATED");
        add(changes, "canonical_url", previous.canonicalUrl(), current.canonicalUrl(), "LINK_CHANGED");
        add(changes, "access_status", previous.accessStatus(), current.accessStatus(), "STATUS_CHANGED");
        add(changes, "language", previous.language(), current.language(), "LANGUAGE_CHANGED");
        add(changes, "duration", previous.duration(), current.duration(), "DURATION_CHANGED");
        add(changes, "format", previous.format(), current.format(), "FORMAT_CHANGED");
        add(changes, "free_status", previous.freeStatus(), current.freeStatus(), "PRICE_CHANGED");
        return List.copyOf(changes);
    }

    private static void add(List<ResourceChange> changes, String field, Object previous, Object current, String type) {
        if (!Objects.equals(previous, current)) {
            changes.add(new ResourceChange(field, stringify(previous), stringify(current), type));
        }
    }

    private static String stringify(Object value) {
        return value == null ? null : value.toString();
    }
}
