package com.siginex.api.config;


import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.service.AuthService;
import com.siginex.api.config.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Resolves the session cookie on each request. It reads
 * {@code __Host-siginex_session}, hashes the opaque token and asks
 * {@link AuthService#validarSesion} to validate it. On success it places a
 * {@link SiginexPrincipal} in the {@link SecurityContextHolder} (authority
 * {@code ROLE_<rol>}) and the tenant id in {@link TenantContext}. On failure it
 * continues the chain unauthenticated and lets the authorization rules respond.
 *
 * <p>The tenant stored in {@link TenantContext} is always cleared at the end of
 * the request so it never leaks into a pooled thread.
 */
@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private static final String SESSION_COOKIE = "__Host-siginex_session";

    private final AuthService authService;
    private final TokenHasher tokenHasher;

    public SessionAuthenticationFilter(AuthService authService, TokenHasher tokenHasher) {
        this.authService = authService;
        this.tokenHasher = tokenHasher;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            readSessionCookie(request)
                    .map(tokenHasher::hash)
                    .flatMap(authService::validarSesion)
                    .ifPresent(this::authenticate);
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private void authenticate(SiginexPrincipal principal) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }
        var authority = new SimpleGrantedAuthority("ROLE_" + principal.rol().toUpperCase());
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, List.of(authority));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        TenantContext.setTenantId(principal.tenantId());
    }

    private static Optional<String> readSessionCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (SESSION_COOKIE.equals(cookie.getName())
                    && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}
