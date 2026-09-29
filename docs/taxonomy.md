# Taxonomía oficial y Guardianes

## 1. Competencias — no modificar

Los filtros de la Biblioteca deben conservar exactamente `Todos` y estos ocho agrupadores:

| ID | Etiqueta | Verbos guía |
|---|---|---|
| `informacion` | 01 · Información | Buscar · evaluar · comprender · organizar |
| `comunicacion` | 02 · Comunicación | Conectar · colaborar · participar |
| `creacion` | 03 · Creación | Crear · producir · programar |
| `seguridad` | 04 · Seguridad | Proteger · prevenir · cuidar |
| `discernimiento` | 05 · Discernimiento | Verificar · contrastar · cuestionar |
| `ia` | 06 · IA | Comprender · experimentar · evaluar |
| `bienestar` | 07 · Bienestar | Equilibrar · autorregular · cuidar |
| `ciudadania` | 08 · Ciudadanía | Participar · respetar · ejercer derechos |

Reglas:

- Cada recurso requiere una competencia primaria.
- Puede tener cero o más secundarias.
- La competencia debe tener explicación y evidencia.
- `Todos` es un filtro de UI, no un valor almacenado.
- No crear nuevas categorías sin una decisión de producto explícita.

## 2. Guardianes oficiales — no modificar

| Nombre exacto | Rol oficial | Territorio | Color | Asset |
|---|---|---|---|---|
| Emi | Agente de Empatía Digital | Empatía y bienestar digital | `#FF6EB6` | `/guardians/emi.png` |
| Locky | Agente de Seguridad Digital | Seguridad y protección | `#0077FF` | `/guardians/locky.png` |
| Lex | Agente de Pensamiento Crítico | Pensamiento crítico y ética | `#FFC857` | `/guardians/lex.png` |
| Byte | Agente de Alfabetización Digital | Aprendizaje e inteligencia digital | `#00C49A` | `/guardians/byte.png` |
| Detective DQ | Agente de Discernimiento Digital | Investigación y verificación | `#6C63FF` | `/guardians/detective-dq.png` |
| Nexo | Agente de Conexión Digital | Colaboración y comunidad | `#00B4D8` | `/guardians/nexo.png` |
| Nova | Agente de Innovación Digital | Innovación y transformación | `#FF7A00` | `/guardians/nova.png` |

Cada recurso tiene `guardian_primary` y opcionalmente `guardian_secondary[]`. El backend no inventa Guardianes, alias, colores o assets.

## 3. Reglas de asignación

La relación competencia–Guardián no se asume por nombre de proveedor. Debe justificarse con contenido, objetivos o metadatos verificados.

Reglas orientativas iniciales, sujetas a validación:

- Seguridad, privacidad y ciberseguridad → Locky.
- Búsqueda, alfabetización y aprendizaje digital → Byte.
- Verificación, fuentes y pensamiento crítico → Detective DQ o Lex.
- IA, innovación y creación tecnológica → Nova.
- Bienestar, convivencia y autorregulación → Emi.
- Comunicación, colaboración y comunidad → Nexo.

Estas reglas generan una propuesta; el agente debe devolver confianza y evidencia. Casos ambiguos van a `REVIEW_REQUIRED`.
