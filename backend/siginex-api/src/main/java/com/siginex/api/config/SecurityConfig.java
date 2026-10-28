package com.siginex.api.config;

import com.siginex.api.config.SessionAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Fase 1 security chain.
 *
 * <p>Public: the Fase 0 healthcheck/info/status endpoints and
 * {@code POST /auth/login} (session bootstrap). Everything else requires an
 * authenticated session, which is established by
 * {@link SessionAuthenticationFilter} from the opaque session cookie.
 *
 * <p>CSRF is enabled with a cookie token readable by the front
 * ({@code CookieCsrfTokenRepository.withHttpOnlyFalse()}) and ignored only for
 * {@code POST /auth/login}, which has no session yet and is protected by
 * SameSite + enumeration resistance + lockout. Sessions are stateless from
 * Spring's point of view: the authentication comes from our cookie, not an
 * {@code HttpSession}. Form login and HTTP Basic are disabled.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SessionAuthenticationFilter sessionAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/auth/login"),
                                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/auth/reset/solicitar"),
                                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/auth/reset/confirmar"),
                                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/v1/plataforma/empresas")))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/api/v1/status")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        // Fase 5: recuperación de contraseña (sin sesión).
                        .requestMatchers(HttpMethod.POST, "/auth/reset/solicitar").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/reset/confirmar").permitAll()
                        // Fase 5: alta de empresa por clave de plataforma (no sesión;
                        // el delegate valida X-Siginex-Admin-Key en tiempo constante).
                        .requestMatchers(HttpMethod.POST, "/api/v1/plataforma/empresas").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());
        return http.build();
    }
}
