package com.siginex.api.auth;

import com.siginex.api.auth.dto.LoginRequest;
import com.siginex.api.auth.dto.MeResponse;
import com.siginex.api.security.SiginexPrincipal;
import com.siginex.api.security.TokenHasher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

/**
 * REST layer for authentication. Validates request format, delegates business
 * work and manages the opaque session cookie. Holds no business logic.
 *
 * <p>Cookie {@code __Host-siginex_session}: HttpOnly, Secure, SameSite=Lax,
 * Path=/. The {@code __Host-} prefix forbids Domain and requires Secure+Path=/.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    static final String SESSION_COOKIE = "__Host-siginex_session";

    private final AuthDelegate authDelegate;
    private final TokenHasher tokenHasher;

    public AuthController(AuthDelegate authDelegate, TokenHasher tokenHasher) {
        this.authDelegate = authDelegate;
        this.tokenHasher = tokenHasher;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AuthService.IssuedSession session = authDelegate.login(
                request.email(), request.password(), httpRequest.getHeader("User-Agent"), httpRequest.getRemoteAddr());
        ResponseCookie cookie = sessionCookie(session.token(),
                Duration.between(Instant.now(), session.expiraEn()));
        return ResponseEntity.noContent()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        Optional<String> token = readSessionCookie(httpRequest);
        token.ifPresent(value -> authDelegate.logout(tokenHasher.hash(value), currentPrincipal()));
        ResponseCookie expired = sessionCookie("", Duration.ZERO);
        return ResponseEntity.noContent()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, expired.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me() {
        SiginexPrincipal principal = currentPrincipal();
        if (principal == null) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new MeResponse(
                principal.usuarioId(), principal.tenantId(), principal.email(), principal.rol()));
    }

    private static SiginexPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SiginexPrincipal principal) {
            return principal;
        }
        return null;
    }

    private static Optional<String> readSessionCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> SESSION_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    private static ResponseCookie sessionCookie(String value, Duration maxAge) {
        return ResponseCookie.from(SESSION_COOKIE, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge.isNegative() ? Duration.ZERO : maxAge)
                .build();
    }
}
