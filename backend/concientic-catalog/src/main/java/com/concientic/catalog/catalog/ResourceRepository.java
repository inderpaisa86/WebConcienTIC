package com.concientic.catalog.catalog;

import com.concientic.catalog.domain.ResourceResponse;
import java.util.List;

public interface ResourceRepository {
    default List<ResourceResponse> search(String query, String competency, String language, String level, String format, int offset, int limit) {
        return search(query, competency, language, level, format, null, offset, limit);
    }

    List<ResourceResponse> search(String query, String competency, String language, String level, String format, String provider, int offset, int limit);

    default long count(String query, String competency, String language, String level, String format) {
        return count(query, competency, language, level, format, null);
    }

    long count(String query, String competency, String language, String level, String format, String provider);
    long countPublished();
}
