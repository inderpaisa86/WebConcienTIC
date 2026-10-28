package com.siginex.api.service;

import com.siginex.api.domain.AuthUser;
import com.siginex.api.domain.SesionInfo;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a datos de autenticación y sesiones: ESTRICTAMENTE lecturas y
 * escrituras a base de datos. No contiene reglas de negocio ni decisiones
 * (bloqueo, enumeración, expiración/revocación, emisión de token): esa
 * orquestación/validación vive en {@link com.siginex.api.delegate.AuthDelegate}.
 * Implementado por {@link AuthServiceImpl}.
 *
 * <p>La búsqueda por email ocurre FUERA de RLS (decisión D4): se realiza antes
 * de conocer el tenant. Aquí eso es solo acceso a datos; la decisión de usarla
 * para autenticar es del Delegate.
 */
public interface AuthService {

    /**
     * LECTURA. Busca el usuario de autenticación por email, fuera de un tenant
     * fijado ({@code runWithoutTenant}). Devuelve el hash y el estado necesarios
     * para que el Delegate decida; no valida nada.
     */
    Optional<AuthUser> buscarAuthPorEmail(String email);

    /** ESCRITURA. Crea una sesión con el hash del token y su expiración. */
    void crearSesion(UUID usuarioId, UUID tenantId, String tokenHash, Instant expiraEn, String userAgent, String ip);

    /** LECTURA. Busca una sesión (unida a su usuario) por el hash del token. */
    Optional<SesionInfo> buscarSesionPorHash(String tokenHash);

    /** ESCRITURA. Revoca la sesión identificada por el hash del token. */
    void revocarSesion(String tokenHash);

    /** ESCRITURA. Incrementa el contador de fallos y fija el bloqueo (puede ser null). */
    void incrementarFallos(UUID usuarioId, Instant bloqueadoHasta);

    /** ESCRITURA. Resetea fallos y fija {@code ultimo_login = now()}. */
    void resetearFallosYLogin(UUID usuarioId);

    /**
     * ESCRITURA. Registra un evento de auditoría. El Delegate decide qué y
     * cuándo auditar; este método solo persiste (el payload no debe contener
     * secretos; el servicio de auditoría los rechaza por diseño).
     */
    void registrarEvento(UUID tenantId, String actor, String accion, String entidad, String entidadId, Map<String, ?> payload);
}
