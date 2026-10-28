package com.siginex.api.service;

import com.siginex.api.domain.AuthUser;
import com.siginex.api.domain.ResetTokenInfo;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Coordinación de datos y correo del flujo de recuperación de contraseña
 * (Fase 5). SOLO BD/APIs; las reglas de negocio (token válido, política de
 * contraseña, no-enumeración) viven en el delegate. Implementado por
 * {@link ResetServiceImpl}.
 */
public interface ResetService {

    /** Busca un usuario por email de forma GLOBAL (fuera de RLS), como el login. */
    Optional<AuthUser> buscarUsuarioPorEmail(String email);

    /**
     * Emite un token: invalida los tokens vigentes del usuario y guarda el nuevo
     * (solo su hash) con expiración. Luego envía el correo con el enlace.
     * El enlace/token nunca se loguean.
     */
    void emitirYEnviar(UUID usuarioId, String tokenHash, Instant expiraEn, String destinatario, String enlaceReset);

    /** Busca un token por su hash (con el tenant del usuario), fuera de RLS. */
    Optional<ResetTokenInfo> buscarTokenPorHash(String tokenHash);

    /**
     * Confirma el reset: marca el token como usado, actualiza el hash de
     * contraseña del usuario y revoca todas sus sesiones. Las escrituras sobre
     * {@code usuario}/{@code sesion} corren bajo el tenant del usuario.
     */
    void confirmar(ResetTokenInfo token, String nuevoPasswordHash);
}
