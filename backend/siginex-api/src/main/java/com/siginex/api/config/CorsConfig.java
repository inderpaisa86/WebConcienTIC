package com.siginex.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS for the Next.js front. Fase 1 adds the session cookie, so credentials
 * must be allowed and the methods used by the authenticated API are exposed.
 * The mapping also covers {@code /auth/**} for login/logout/me.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final String allowedOrigins;

    public CorsConfig(
            @Value("${siginex.cors.allowed-origins:https://concientic.com,http://localhost:3000}") String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "X-XSRF-TOKEN")
                .allowCredentials(true)
                .maxAge(3600);
        registry.addMapping("/auth/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("Content-Type", "X-XSRF-TOKEN")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
