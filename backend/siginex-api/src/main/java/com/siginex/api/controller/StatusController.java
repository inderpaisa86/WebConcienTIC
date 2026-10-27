package com.siginex.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal, database-free status endpoint for the Fase 0 skeleton.
 *
 * <p>It exists only to exercise the {@code /api/**} CORS mapping and to seed the
 * layered {@code api} package. It holds no business logic; real controllers and
 * their service/repository layers arrive in later phases.
 */
@RestController
@RequestMapping("/api/v1")
public class StatusController {

    private final String applicationName;

    public StatusController(@Value("${spring.application.name:siginex-api}") String applicationName) {
        this.applicationName = applicationName;
    }

    @GetMapping("/status")
    public StatusResponse status() {
        return new StatusResponse("ok", applicationName);
    }
}
