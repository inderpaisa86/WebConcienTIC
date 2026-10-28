package com.siginex.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Implementación de {@link EmailSender} por defecto (Fase 5): NO envía correo
 * real. Se activa cuando {@code siginex.email.provider} no es {@code resend}
 * (valor por defecto {@code stub}), de modo que el flujo de recuperación queda
 * completo y testeable sin credenciales.
 *
 * <p>Seguridad: registra únicamente que se enviaría un correo a un destinatario
 * (email), SIN el token ni el enlace de recuperación. El enlace lleva el token
 * en claro y nunca debe aparecer en logs.
 */
@Service
@ConditionalOnProperty(name = "siginex.email.provider", havingValue = "stub", matchIfMissing = true)
@Slf4j
public class StubEmailSender implements EmailSender {

    @Override
    public void enviarRecuperacion(String destinatario, String enlaceReset) {
        // NUNCA loguear `enlaceReset` (contiene el token en claro).
        log.info("[email:stub] se enviaria correo de recuperacion a {}", destinatario);
    }
}
