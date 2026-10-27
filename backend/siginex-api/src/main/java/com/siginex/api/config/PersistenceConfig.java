package com.siginex.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Exposes a {@link TransactionTemplate} bean on top of the
 * {@link PlatformTransactionManager} that Spring Boot auto-configures for the
 * JDBC {@code DataSource}. The tenant plumbing uses it to run each RLS-scoped
 * unit of work inside a single transaction.
 */
@Configuration
public class PersistenceConfig {

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
