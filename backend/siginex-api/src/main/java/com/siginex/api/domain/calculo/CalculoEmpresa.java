package com.siginex.api.domain.calculo;

import lombok.Builder;
import lombok.Value;

/**
 * Perfil de la organización que consume el motor. Análogo al
 * {@code OrquestadorCompany} del TS. Inmutable con Lombok
 * {@code @Value @Builder} (convención del proyecto: sin {@code record}).
 *
 * <p>{@code nivelTecnologico} es {@link Integer} (1..5) o {@code null}: el
 * benchmark dentro de {@code run()} solo se calcula cuando no es {@code null}.
 */
@Value
@Builder
public class CalculoEmpresa {

    String nombre;
    String sector;
    String tamano;
    Integer nivelTecnologico;
    Aplicabilidad aplicabilidad;
}
