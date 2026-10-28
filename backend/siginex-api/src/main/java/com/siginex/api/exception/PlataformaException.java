package com.siginex.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza en el flujo de plataforma (alta de empresa, Fase 5) cuando una regla
 * de negocio o la autorización por clave de plataforma falla. Lleva el
 * {@link HttpStatus} que la API debe devolver para que el handler lo mapee sin
 * adivinar: 401 cuando la clave de plataforma es inválida o ausente, 409 en
 * slug/email duplicado, 503 cuando la clave de plataforma no está configurada.
 * Mismo patrón que {@link UsuarioException} y {@link DominioException}.
 */
public class PlataformaException extends RuntimeException {

    private final HttpStatus status;

    public PlataformaException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
