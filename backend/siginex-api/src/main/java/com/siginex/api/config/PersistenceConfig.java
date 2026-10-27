package com.siginex.api.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

    /**
     * Jackson 2 {@link ObjectMapper} used by the JDBC repositories and services
     * to read/write {@code jsonb} columns (e.g. {@code kb_pregunta.recomendacion},
     * audit payloads, {@code resultado_pilar.hallazgos}).
     *
     * <p>Spring Boot 4 moved its JSON auto-configuration to Jackson 3, so it
     * auto-configures a {@code tools.jackson.databind.JsonMapper} bean, NOT a
     * {@code com.fasterxml.jackson.databind.ObjectMapper}. All of this code base
     * is written against Jackson 2's {@code ObjectMapper}, so we declare that
     * bean here explicitly. {@code @ConditionalOnMissingBean} keeps it a no-op if
     * an {@code ObjectMapper} is ever contributed elsewhere.
     */
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
