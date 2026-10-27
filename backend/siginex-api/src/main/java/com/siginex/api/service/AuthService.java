package com.siginex.api.service;

import com.siginex.api.domain.SiginexPrincipal;

import java.time.Instant;
import java.util.Optional;

/**
 * Authentication use cases: coordinates password verification, lockout, session
 * issuing/validation/revocation and auditing. Implemented by
 * {@link AuthServiceImpl}.
 */
public interface AuthService {

    /**
     * Authenticates by email + password and, on success, issues an opaque
     * session token.
     *
     * @return the plaintext opaque token and its absolute expiry.
     * @throws com.siginex.api.exception.AuthException with a generic message on any failure.
     */
    IssuedSession autenticar(String email, String password, String userAgent, String ip);

    /**
     * Validates a session by the SHA-256 hash of its opaque token. Returns the
     * principal only when the session exists, is not revoked, has not expired
     * and the user is active.
     */
    Optional<SiginexPrincipal> validarSesion(String tokenHash);

    /** Revokes the session identified by the token hash and audits the logout. */
    void revocar(String tokenHash, SiginexPrincipal principal);

    /** Opaque token (plaintext, for the cookie) plus its absolute expiry. */
    record IssuedSession(String token, Instant expiraEn) {
    }
}
