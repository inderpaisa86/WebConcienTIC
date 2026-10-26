package com.siginex.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * PROVISIONAL minimal security chain for the Fase 0 skeleton.
 *
 * <p>{@code spring-boot-starter-security} secures every endpoint by default and
 * returns 401 (with a generated HTTP Basic / login form) on
 * {@code /actuator/health}, which would break the healthcheck that is the whole
 * point of Fase 0. This chain opens only the healthcheck, the actuator info
 * endpoint and the read-only status endpoint, and leaves everything else
 * authenticated.
 *
 * <p>It defines NO users, NO password encoder and NO login endpoints. Real
 * authentication and authorization rules are introduced in Fase 1, which
 * replaces this class.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/api/v1/status")
                        .permitAll()
                        .anyRequest().authenticated())
                .httpBasic(basic -> {
                })
                .formLogin(form -> form.disable());
        return http.build();
    }
}
