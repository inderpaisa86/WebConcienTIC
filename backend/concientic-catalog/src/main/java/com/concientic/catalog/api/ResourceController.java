package com.concientic.catalog.api;

import com.concientic.catalog.catalog.CatalogService;
import com.concientic.catalog.domain.ResourceResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController {
    private final CatalogService catalogService;

    public ResourceController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public PageResponse<ResourceResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String competency,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String format,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        return catalogService.search(q, competency, language, level, format, page, size);
    }
}
