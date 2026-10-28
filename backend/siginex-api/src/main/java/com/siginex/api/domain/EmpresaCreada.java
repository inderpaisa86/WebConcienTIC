package com.siginex.api.domain;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Resultado del alta de empresa (Fase 5): el tenant recién creado junto con el
 * identificador de su primer usuario admin. No lleva ningún secreto (ni el hash
 * de la contraseña del admin ni token alguno).
 */
@Value
@Builder
public class EmpresaCreada {

    UUID tenantId;
    String tenantNombre;
    String tenantSlug;
    UUID adminUsuarioId;
    String adminEmail;
}
