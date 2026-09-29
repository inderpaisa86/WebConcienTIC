package com.concientic.catalog.api;

import com.concientic.catalog.catalog.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogStatusController {
    private final CatalogService catalogService;

    public CatalogStatusController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/status")
    public CatalogStatusResponse status() {
        return new CatalogStatusResponse(null, null, catalogService.countPublished(), null);
    }
}
