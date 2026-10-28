package com.siginex.api.service;

import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.domain.AuthUser;
import com.siginex.api.domain.SesionInfo;
import com.siginex.api.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación por defecto de {@link AuthService}: SOLO acceso a datos. Tras
 * el refactor de capas (alineación a la regla de oro), toda la lógica de negocio
 * de autenticación —bloqueo por intentos, resistencia a enumeración (dummy
 * hash / timing), verificación de credenciales, emisión del token, y las reglas
 * de validez de sesión (revocada/expirada/usuario activo)— vive en
 * {@link com.siginex.api.delegate.AuthDelegate}. Aquí solo quedan lecturas y
 * escrituras.
 *
 * <p>{@link #buscarAuthPorEmail} corre en {@code runWithoutTenant} (decisión
 * D4): la búsqueda de login ocurre antes de conocer el tenant. La auditoría se
 * delega en {@link AuditoriaService}, que rechaza payloads con claves secretas.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final AuditoriaService auditoriaService;
    private final TenantTransactionTemplate tenantTx;

    @Override
    public Optional<AuthUser> buscarAuthPorEmail(String email) {
        return tenantTx.runWithoutTenant(() -> authRepository.findAuthByEmail(email));
    }

    @Override
    public void crearSesion(UUID usuarioId, UUID tenantId, String tokenHash, Instant expiraEn, String userAgent, String ip) {
        authRepository.crearSesion(usuarioId, tenantId, tokenHash, expiraEn, userAgent, ip);
    }

    @Override
    public Optional<SesionInfo> buscarSesionPorHash(String tokenHash) {
        return authRepository.buscarSesionPorHash(tokenHash);
    }

    @Override
    public void revocarSesion(String tokenHash) {
        authRepository.revocarSesion(tokenHash);
    }

    @Override
    public void incrementarFallos(UUID usuarioId, Instant bloqueadoHasta) {
        authRepository.incrementarFallos(usuarioId, bloqueadoHasta);
    }

    @Override
    public void resetearFallosYLogin(UUID usuarioId) {
        authRepository.resetearFallosYLogin(usuarioId);
    }

    @Override
    public void registrarEvento(UUID tenantId, String actor, String accion, String entidad, String entidadId, Map<String, ?> payload) {
        auditoriaService.registrar(tenantId, actor, accion, entidad, entidadId, payload);
    }
}
