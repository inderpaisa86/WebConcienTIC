package com.siginex.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.flyway.enabled=false",
                "spring.datasource.url=jdbc:postgresql://localhost:5432/disabled-for-wiring-test"
        })
class ApplicationContextWiringTest {

    @Test
    void startsSpringContextWithoutNeonConnection() {
        // If constructor wiring is broken, the test context fails before this assertion.
        assertThat(true).isTrue();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class WiringTestConfiguration {
        @Bean
        DataSource dataSource() {
            return mock(DataSource.class);
        }

        @Bean
        NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
            return new NamedParameterJdbcTemplate(mock(JdbcOperations.class));
        }
    }
}
