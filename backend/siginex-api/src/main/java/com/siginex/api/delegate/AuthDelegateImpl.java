package com.siginex.api.delegate;

import com.siginex.api.exception.AuthException;
import com.siginex.api.service.AuthService;
import com.siginex.api.domain.SiginexPrincipal;
import org.springframework.stereotype.Component;

/**
 * Default {@link AuthDelegate} implementation.
 *
 * <p>The account-state rules (active, not locked) are enforced inside
 * {@link AuthService#autenticar} so they share the single, constant-time,
 * enumeration-resistant path and always surface the same generic
 * {@link AuthException}. The delegate keeps that invariant and does not add a
 * second, observably-different branch.
 */
@Component
public class AuthDelegateImpl implements AuthDelegate {

    private final AuthService authService;

    public AuthDelegateImpl(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public AuthService.IssuedSession login(String email, String password, String userAgent, String ip) {
        return authService.autenticar(email, password, userAgent, ip);
    }

    @Override
    public void logout(String tokenHash, SiginexPrincipal principal) {
        authService.revocar(tokenHash, principal);
    }
}
