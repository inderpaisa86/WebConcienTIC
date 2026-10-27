package com.siginex.api.domain.calculo;

import com.siginex.api.domain.KbModulo;
import lombok.Getter;

import java.util.List;

/**
 * Módulo materializado para el motor de cálculo. Envuelve un {@link KbModulo},
 * parsea su {@code weight} a {@code double} <b>una sola vez</b> y conserva sus
 * preguntas en el orden del KB.
 *
 * <p>{@code nombre} replica el fallback del TS {@code m.nombre ?? m.short ?? m.id}
 * (ver {@link #nombreResuelto()}). El peso se parsea con
 * {@link Double#parseDouble(String)}; un peso no parseable lanza
 * {@link NumberFormatException} aquí (KB corrupto).
 */
@Getter
public class CalculoModulo {

    private final String id;
    private final String nombre;
    private final String shortNombre;
    private final double peso;
    private final List<CalculoPregunta> preguntas;

    public CalculoModulo(KbModulo modulo, List<CalculoPregunta> preguntas) {
        this.id = modulo.getId();
        this.nombre = modulo.getNombre();
        this.shortNombre = modulo.getShortNombre();
        // Parseo único del peso String->double; falla aquí si el KB está corrupto.
        this.peso = Double.parseDouble(modulo.getWeight());
        this.preguntas = preguntas;
    }

    /**
     * Nombre del módulo con el fallback del TS: {@code nombre ?? short ?? id}.
     *
     * @return {@code nombre} si no es {@code null}; si no {@code shortNombre} si
     *         no es {@code null}; en último caso {@code id}.
     */
    public String nombreResuelto() {
        if (nombre != null) {
            return nombre;
        }
        if (shortNombre != null) {
            return shortNombre;
        }
        return id;
    }
}
