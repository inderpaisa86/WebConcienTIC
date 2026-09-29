package com.concientic.catalog.catalog;

import com.concientic.catalog.api.PageResponse;
import com.concientic.catalog.domain.ResourceResponse;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {
    private static final int MAX_PAGE_SIZE = 100;
    private final ResourceRepository repository;

    public CatalogService(ResourceRepository repository) {
        this.repository = repository;
    }

    public PageResponse<ResourceResponse> search(String query, String competency, String language, String level, String format, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        long total = repository.count(query, competency, language, level, format);
        return PageResponse.of(repository.search(query, competency, language, level, format, safePage * safeSize, safeSize), safePage, safeSize, total);
    }

    public long countPublished() {
        return repository.countPublished();
    }
}
