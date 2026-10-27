package com.siginex.api.usuario;

import org.springframework.http.HttpStatus;

/**
 * Raised by {@link UsuarioDelegate} when a business rule or authorization check
 * fails. It carries the {@link HttpStatus} the API must return so the handler
 * maps it without guessing: 403 when the actor is not an admin, 409 on a
 * duplicate email, 404 when the target user is not in the admin's tenant.
 */
public class UsuarioException extends RuntimeException {

    private final HttpStatus status;

    public UsuarioException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
