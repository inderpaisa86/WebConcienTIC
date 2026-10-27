package com.siginex.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Raised by the Fase 2b domain layers (organizacion, diagnostico) when a
 * business rule fails or an entity does not exist in / belong to the actor's
 * tenant. It carries the {@link HttpStatus} the API must return so the handler
 * maps it without guessing (typically 404 when the entity is not found under
 * the actor's tenant). Same pattern as {@link UsuarioException}.
 */
public class DominioException extends RuntimeException {

    private final HttpStatus status;

    public DominioException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
