package com.siginex.api.config;

import com.siginex.api.exception.AuthException;
import com.siginex.api.exception.UsuarioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Maps application exceptions to HTTP responses.
 *
 * <p>{@link AuthException} always becomes a 401 with the same generic body,
 * regardless of the real cause, so responses never reveal whether an email
 * exists, is inactive or is locked. {@link UsuarioException} carries its own
 * status (403/404/409). Bean validation failures become 400.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuth(AuthException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "credenciales invalidas"));
    }

    @ExceptionHandler(UsuarioException.class)
    public ResponseEntity<Map<String, String>> handleUsuario(UsuarioException ex) {
        return ResponseEntity.status(ex.status())
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "solicitud invalida"));
    }
}
