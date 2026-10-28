package com.siginex.api.delegate;

import com.siginex.api.dto.reset.ConfirmarResetRequest;
import com.siginex.api.dto.reset.SolicitarResetRequest;

/**
 * Reglas de negocio del flujo de recuperación de contraseña (Fase 5). El
 * controller delega aquí tras validar el formato. Implementado por
 * {@link ResetDelegateImpl}.
 *
 * <p>Seguridad: {@link #solicitar} NO revela si el email existe (sin
 * enumeración) y mantiene tiempos uniformes. {@link #confirmar} valida que el
 * token exista, no esté usado, no haya expirado y su hash coincida; aplica la
 * política de contraseña; re-hashea con Argon2id; marca el token usado y revoca
 * las sesiones del usuario. Ni el token ni el enlace se loguean.
 */
public interface ResetDelegate {

    /** Solicita un reset. Siempre termina igual (sin señal de existencia). */
    void solicitar(SolicitarResetRequest request);

    /** Confirma el reset con el token del enlace y la nueva contraseña. */
    void confirmar(ConfirmarResetRequest request);
}
