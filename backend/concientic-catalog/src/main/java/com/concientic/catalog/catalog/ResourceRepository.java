package com.concientic.catalog.catalog;

import com.concientic.catalog.domain.ResourceResponse;
import java.util.List;

public interface ResourceRepository {
    List<ResourceResponse> search(String query, String competency, String language, String level, String format, int offset, int limit);
    long count(String query, String competency, String language, String level, String format);
    long countPublished();
}
