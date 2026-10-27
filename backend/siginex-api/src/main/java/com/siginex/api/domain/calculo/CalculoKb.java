package com.siginex.api.domain.calculo;

import com.siginex.api.domain.KbModulo;
import com.siginex.api.domain.KbPregunta;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * KB de cálculo materializado: las 9 definiciones de módulo (en el orden del KB)
 * con sus preguntas y los pesos ya parseados a {@code double}. Estructura de
 * dominio dedicada que el motor consume; <b>no toca la BD</b> (se construye a
 * partir de los {@link KbModulo}/{@link KbPregunta} ya leídos por la capa de
 * repositorio).
 *
 * <p>Expone {@link #pesoModulo(String)} O(1) por id. El parseo de pesos ocurre
 * al materializar (en {@link CalculoModulo}/{@link CalculoPregunta}); si un peso
 * no fuese parseable, falla aquí de forma ruidosa (KB corrupto).
 */
@Getter
public class CalculoKb {

    private final List<CalculoModulo> modulos;
    private final Map<String, Double> pesoPorModulo;

    private CalculoKb(List<CalculoModulo> modulos) {
        this.modulos = modulos;
        // Índice id -> peso, en el orden del KB (LinkedHashMap preserva el orden).
        Map<String, Double> pesos = new LinkedHashMap<>();
        for (CalculoModulo m : modulos) {
            pesos.put(m.getId(), m.getPeso());
        }
        this.pesoPorModulo = pesos;
    }

    /**
     * Materializa el KB de cálculo a partir de los módulos del KB (en orden) y
     * una función que, por id de módulo, devuelve sus preguntas (en orden).
     *
     * @param kbModulos    módulos del KB en el orden de recorrido (columna {@code orden}).
     * @param preguntasPor función {@code moduloId -> preguntas ordenadas}.
     * @return el KB de cálculo con pesos ya parseados.
     */
    public static CalculoKb from(List<KbModulo> kbModulos,
                                 Function<String, List<KbPregunta>> preguntasPor) {
        List<CalculoModulo> mods = new ArrayList<>(kbModulos.size());
        for (KbModulo m : kbModulos) {
            List<KbPregunta> preguntas = preguntasPor.apply(m.getId());
            List<CalculoPregunta> cp = new ArrayList<>(preguntas.size());
            for (KbPregunta q : preguntas) {
                cp.add(new CalculoPregunta(q));
            }
            mods.add(new CalculoModulo(m, cp));
        }
        return new CalculoKb(mods);
    }

    /**
     * Peso del módulo por id (ya parseado a {@code double}).
     *
     * @param moduloId id del módulo.
     * @return el peso como {@code double}.
     * @throws IllegalArgumentException si el módulo no existe en el KB.
     */
    public double pesoModulo(String moduloId) {
        Double peso = pesoPorModulo.get(moduloId);
        if (peso == null) {
            throw new IllegalArgumentException("Módulo no encontrado en el KB: " + moduloId);
        }
        return peso;
    }
}
