package com.siginex.api.service;

import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.domain.AuthUser;
import com.siginex.api.domain.ResetTokenInfo;
import com.siginex.api.repository.AuthRepository;
import com.siginex.api.repository.ResetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación por defecto de {@link ResetService}.
 *
 * <p>Aislamiento: la búsqueda de usuario por email y la de token por hash
 * corren FUERA de RLS ({@code runWithoutTenant}), igual que el login
 * (decisión D4). La emisión del token también va fuera de RLS (reset_password
 * no tiene política por tenant, como sesion). En cambio, al confirmar, las
 * escrituras sobre {@code usuario} (nuevo hash) y {@code sesion} (revocar) van
 * BAJO el tenant del usuario ({@code runInTenant}) para satisfacer la política
 * RLS de {@code usuario}.
 *
 * <p>No loguea el token ni el enlace. El hash de contraseña ya viene calculado.
 */
@Service
@RequiredArgsConstructor
public class ResetServiceImpl implements ResetService {

    private final ResetRepository resetRepository;
    private final AuthRepository authRepository;
    private final EmailSender emailSender;
    private final TenantTransactionTemplate tenantTx;

    @Override
    public Optional<AuthUser> buscarUsuarioPorEmail(String email) {
        return tenantTx.runWithoutTenant(() -> authRepository.findAuthByEmail(email));
    }

    @Override
    public void emitirYEnviar(UUID usuarioId, String tokenHash, Instant expiraEn,
                              String destinatario, String enlaceReset) {
        tenantTx.runWithoutTenant(() -> {
            resetRepository.invalidarVigentesDeUsuario(usuarioId);
            resetRepository.insertar(usuarioId, tokenHash, expiraEn);
            return null;
        });
        // Envío fuera de la transacción de BD. Un fallo de correo se propaga,
        // pero el delegate NO lo revela al cliente (no-enumeración).
        emailSender.enviarRecuperacion(destinatario, enlaceReset);
    }

    @Override
    public Optional<ResetTokenInfo> buscarTokenPorHash(String tokenHash) {
        return tenantTx.runWithoutTenant(() -> resetRepository.buscarPorHash(tokenHash));
    }

    @Override
    public void confirmar(ResetTokenInfo token, String nuevoPasswordHash) {
        // Marcar usado puede ir fuera de RLS (reset_password no tiene política).
        tenantTx.runWithoutTenant(() -> {
            resetRepository.marcarUsado(token.getId());
            return null;
        });
        // El UPDATE de usuario está bajo FORCE RLS: debe correr con el tenant
        // del usuario fijado para que la política lo permita. Revocar sesiones
        // va en la misma transacción del tenant.
        tenantTx.runInTenant(token.getTenantId(), () -> {
            authRepository.actualizarPasswordHash(token.getUsuarioId(), nuevoPasswordHash);
            authRepository.revocarTodasLasSesiones(token.getUsuarioId());
            return null;
        });
    }
}
