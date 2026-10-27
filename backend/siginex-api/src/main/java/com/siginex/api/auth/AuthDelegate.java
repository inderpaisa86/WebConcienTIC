package com.siginex.api.auth;

import com.siginex.api.security.SiginexPrincipal;
import org.springframework.stereotype.Component;

/**
 * Business orchestration for authentication. Sits between the controller and
 * the service: it decides whether a use case may proceed and delegates the data
 * work to {@link AuthService}.
 *
 * <p>The account-state rules (active, not locked) are enforced inside
 * {@link AuthService#autenticar} so they share the single, constant-time,
 * enumeration-resistant path and always surface the same generic
 * {@link AuthException}. The delegate keeps that invariant and does not add a
 * second, observably-different branch.
 */
@Component
public class AuthDelegate {

    private final AuthService authService;

    public AuthDelegate(AuthService authService) {
        this.authService = authService;
    }

    public AuthService.IssuedSession login(String email, String password, String userAgent, String ip) {
        return authService.autenticar(email, password, userAgent, ip);
    }

    public void logout(String tokenHash, SiginexPrincipal principal) {
        authService.revocar(tokenHash, principal);
    }
}
