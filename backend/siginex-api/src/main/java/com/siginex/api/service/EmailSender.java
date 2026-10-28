package com.siginex.api.service;

/**
 * Envío de correo transaccional. Abstracción para que el flujo de recuperación
 * (Fase 5) no dependa de un proveedor concreto. Dos implementaciones:
 * {@code StubEmailSender} (por defecto, registra que se enviaría SIN el token)
 * y {@code ResendEmailSender} (API HTTP de Resend, activable por config).
 *
 * <p>Regla de seguridad: ninguna implementación puede loguear el enlace de
 * recuperación completo (lleva el token en claro) ni el token. El enlace solo
 * se entrega al destinatario por correo.
 */
public interface EmailSender {

    /**
     * Envía el correo de recuperación de contraseña.
     *
     * @param destinatario email del usuario.
     * @param enlaceReset  URL completa con el token (solo para el cuerpo del
     *                     correo; NUNCA se loguea).
     */
    void enviarRecuperacion(String destinatario, String enlaceReset);
}
