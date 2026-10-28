package com.siginex.api.controller;

import com.siginex.api.delegate.ResetDelegate;
import com.siginex.api.dto.reset.ConfirmarResetRequest;
import com.siginex.api.dto.reset.SolicitarResetRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Capa REST de la recuperación de contraseña (Fase 5). Sin lógica de negocio:
 * valida el formato y delega. Ambas rutas son públicas (no hay sesión) y con
 * CSRF ignorado, como {@code POST /auth/login}.
 *
 * <p>{@code POST /auth/reset/solicitar} responde SIEMPRE 204, exista o no el
 * email (no-enumeración). {@code POST /auth/reset/confirmar} devuelve 204 al
 * completar; un token inválido/usado/expirado produce 401 genérico.
 */
@RestController
@RequestMapping("/auth/reset")
@RequiredArgsConstructor
public class ResetController {

    private final ResetDelegate resetDelegate;

    @PostMapping("/solicitar")
    public ResponseEntity<Void> solicitar(@Valid @RequestBody SolicitarResetRequest request) {
        resetDelegate.solicitar(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirmar")
    public ResponseEntity<Void> confirmar(@Valid @RequestBody ConfirmarResetRequest request) {
        resetDelegate.confirmar(request);
        return ResponseEntity.noContent().build();
    }
}
