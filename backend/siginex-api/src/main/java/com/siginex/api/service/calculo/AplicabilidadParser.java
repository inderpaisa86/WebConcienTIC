package com.siginex.api.service.calculo;

import com.siginex.api.domain.calculo.Aplicabilidad;

import java.util.Map;

/**
 * Parser del jsonb {@code organizacion.aplicabilidad} a {@link Aplicabilidad}.
 * Replica la semántica del TS:
 *
 * <pre>
 * const parsed = aplicabilidadSchema.safeParse(org.aplicabilidad ?? {});
 * const aplicabilidad = { ...APLICABILIDAD_DEFAULT, ...(parsed.success ? parsed.data : {}) };
 * </pre>
 *
 * con {@code APLICABILIDAD_DEFAULT} = los 4 flags en {@code true} y un schema zod
 * de 4 booleanos parciales. Reglas (estilo zod):
 * <ul>
 *   <li>{@code null} / mapa vacío → los 4 defaults en {@code true}.</li>
 *   <li>clave presente con booleano → sobrescribe ese flag.</li>
 *   <li>clave reconocida con valor NO booleano → el {@code safeParse} falla ⇒ se
 *       descarta todo el objeto y quedan los 4 defaults en {@code true}.</li>
 *   <li>claves extra desconocidas → se ignoran sin fallar.</li>
 * </ul>
 * Clase de utilidad, no instanciable.
 */
public final class AplicabilidadParser {

    private static final String[] CLAVES = {"permAmbiental", "saglaft", "ptee", "rnbd"};

    private AplicabilidadParser() {
    }

    /**
     * Parsea un mapa (jsonb deserializado) a {@link Aplicabilidad}.
     *
     * @param jsonb mapa de la columna jsonb; {@code null} o vacío → 4 defaults true.
     * @return la aplicabilidad resuelta.
     */
    public static Aplicabilidad parse(Map<String, Object> jsonb) {
        // Default: perfil más exigente (los 4 flags en true).
        boolean permAmbiental = true;
        boolean saglaft = true;
        boolean ptee = true;
        boolean rnbd = true;

        if (jsonb == null || jsonb.isEmpty()) {
            return build(permAmbiental, saglaft, ptee, rnbd);
        }

        // Validación estilo zod: si cualquier clave reconocida tiene valor NO
        // booleano, safeParse falla y se descarta TODO el objeto (4 defaults true).
        for (String clave : CLAVES) {
            if (jsonb.containsKey(clave) && !(jsonb.get(clave) instanceof Boolean)) {
                return build(true, true, true, true);
            }
        }

        // Merge: solo las claves reconocidas presentes y booleanas sobrescriben.
        if (jsonb.get("permAmbiental") instanceof Boolean b) {
            permAmbiental = b;
        }
        if (jsonb.get("saglaft") instanceof Boolean b) {
            saglaft = b;
        }
        if (jsonb.get("ptee") instanceof Boolean b) {
            ptee = b;
        }
        if (jsonb.get("rnbd") instanceof Boolean b) {
            rnbd = b;
        }

        return build(permAmbiental, saglaft, ptee, rnbd);
    }

    private static Aplicabilidad build(boolean permAmbiental, boolean saglaft,
                                       boolean ptee, boolean rnbd) {
        return Aplicabilidad.builder()
                .permAmbiental(permAmbiental)
                .saglaft(saglaft)
                .ptee(ptee)
                .rnbd(rnbd)
                .build();
    }
}
