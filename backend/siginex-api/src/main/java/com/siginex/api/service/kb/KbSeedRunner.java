package com.siginex.api.service.kb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Runs the idempotent KB seed once at application startup. Delegates all work
 * to {@link KbSeedService} (which is also unit/integration testable on its own).
 *
 * <p>Guarded by {@code siginex.kb.seed.enabled} (default on,
 * {@code matchIfMissing = true}). It is turned off in the context-wiring test,
 * which boots the Spring context with a mocked {@code DataSource} and
 * {@code spring.flyway.enabled=false}; without this guard the runner would hit
 * that mocked datasource at startup and fail the wiring test.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "siginex.kb.seed.enabled", havingValue = "true", matchIfMissing = true)
public class KbSeedRunner implements ApplicationRunner {

    private final KbSeedService kbSeedService;

    @Override
    public void run(ApplicationArguments args) {
        kbSeedService.seed();
    }
}
