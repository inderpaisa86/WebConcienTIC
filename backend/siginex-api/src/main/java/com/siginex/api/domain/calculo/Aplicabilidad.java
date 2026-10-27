package com.siginex.api.domain.calculo;

import lombok.Builder;
import lombok.Value;

/**
 * Perfil de aplicabilidad de la organización. Port 1:1 del tipo TS
 * {@code Aplicabilidad} (4 flags booleanos). Inmutable con Lombok
 * {@code @Value @Builder} (convención del proyecto: sin {@code record}).
 *
 * <p>El default del orquestador ({@code APLICABILIDAD_DEFAULT}) deja los 4 flags
 * en {@code true} (perfil más exigente); las exclusiones de
 * {@code aplicabilidad.ts::aplica} solo actúan cuando un flag se desactiva.
 */
@Value
@Builder
public class Aplicabilidad {

    boolean permAmbiental;
    boolean saglaft;
    boolean ptee;
    boolean rnbd;
}
