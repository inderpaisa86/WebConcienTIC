package com.siginex.api.service;

import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.domain.EmpresaCreada;
import com.siginex.api.domain.Usuario;
import com.siginex.api.repository.AuthRepository;
import com.siginex.api.repository.TenantRepository;
import com.siginex.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementación por defecto de {@link PlataformaService}.
 *
 * <p>Aislamiento: el tenant se crea con {@code runWithoutTenant} (la tabla
 * {@code tenant} no tiene RLS y el tenant aún no existe). El primer usuario
 * admin se inserta con {@code runInTenant(nuevoTenantId, ...)}, de modo que la
 * política RLS de {@code usuario} se satisface ({@code tenant_id =
 * current_tenant()}). La comprobación de email es GLOBAL y usa
 * {@code findAuthByEmail} fuera de RLS (igual que el login, decisión D4).
 *
 * <p>Esta capa no valida nada ni autoriza: recibe del delegate el hash ya
 * calculado y el slug ya resuelto. Nunca loguea la contraseña ni el hash.
 */
@Service
@RequiredArgsConstructor
public class PlataformaServiceImpl implements PlataformaService {

    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthRepository authRepository;
    private final TenantTransactionTemplate tenantTx;

    @Override
    public boolean existeSlug(String slug) {
        return tenantTx.runWithoutTenant(() -> tenantRepository.existeSlug(slug));
    }

    @Override
    public boolean existeEmailGlobal(String email) {
        return tenantTx.runWithoutTenant(() -> authRepository.findAuthByEmail(email).isPresent());
    }

    @Override
    public EmpresaCreada crearEmpresaConAdmin(String nombre, String slug, Usuario adminAInsertar) {
        // 1) Crear el tenant fuera de RLS (aún no hay tenant que fijar).
        EmpresaCreada tenant = tenantTx.runWithoutTenant(
                () -> tenantRepository.insertarTenant(nombre, slug));

        // 2) Insertar el admin BAJO el tenant recién creado: así la política RLS
        //    (tenant_id = current_tenant()) permite la fila. El usuario a
        //    insertar ya trae el tenantId del tenant creado y el hash Argon2id.
        Usuario conTenant = new Usuario(
                null,
                tenant.getTenantId(),
                adminAInsertar.email(),
                adminAInsertar.nombre(),
                adminAInsertar.passwordHash(),
                adminAInsertar.rol(),
                true,
                null);

        Usuario admin = tenantTx.runInTenant(tenant.getTenantId(),
                () -> usuarioRepository.insertar(conTenant));

        return EmpresaCreada.builder()
                .tenantId(tenant.getTenantId())
                .tenantNombre(tenant.getTenantNombre())
                .tenantSlug(tenant.getTenantSlug())
                .adminUsuarioId(admin.id())
                .adminEmail(admin.email())
                .build();
    }
}
