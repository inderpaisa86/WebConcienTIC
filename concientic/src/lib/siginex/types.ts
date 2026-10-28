/**
 * Tipos del contrato de autenticación de SIGINEX.
 *
 * Reflejan los DTO del backend Java (`backend/siginex-api`):
 * - `POST /auth/login`  → body `LoginRequest { email, password }`, respuesta 204.
 * - `POST /auth/logout` → 204, expira la cookie de sesión.
 * - `GET  /auth/me`     → 200 `MeResponse { usuarioId, tenantId, email, rol }`,
 *                          401 si no hay sesión válida.
 *
 * Los UUID del backend se representan como `string` en el cliente.
 */

/** Body de `POST /auth/login`. */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * Rol del usuario. El backend devuelve un `String` libre en `MeResponse.rol`;
 * aquí se tipa de forma abierta para no romper ante valores no previstos.
 */
export type Rol = string;

/**
 * Respuesta de `GET /auth/me` ya normalizada para el cliente.
 * Coincide con `MeResponse` del backend (UUID → string).
 */
export interface Me {
  usuarioId: string;
  tenantId: string;
  email: string;
  rol: Rol;
}

/** Forma cruda del JSON de `MeResponse` tal como lo emite el backend. */
export interface MeResponsePayload {
  usuarioId: string;
  tenantId: string;
  email: string;
  rol: string;
}

/* ------------------------------------------------------------------ *
 * Fase 5: gestión de usuarios por el admin. Reflejan los DTO del
 * backend Java:
 * - `UsuarioResponse`      { UUID id, UUID tenantId, String email,
 *                            String nombre, String rol, boolean activo,
 *                            Instant creadoEn }
 * - `CrearUsuarioRequest`  { @Email email, @NotBlank nombre,
 *                            @Size(min=12) password, @Pattern(admin|miembro) rol }
 * ------------------------------------------------------------------ */

/** Usuario tal como lo devuelve `GET/POST /api/v1/usuarios` (sin secretos). */
export interface UsuarioResumen {
  id: string;
  tenantId: string;
  email: string;
  nombre: string;
  rol: Rol;
  activo: boolean;
  creadoEn: string;
}

/** Body de `POST /api/v1/usuarios` (crear usuario en el tenant del admin). */
export interface CrearUsuarioRequest {
  email: string;
  nombre: string;
  password: string;
  rol: "admin" | "miembro";
}

/* ------------------------------------------------------------------ *
 * Sub-fase 3b: tipos del contrato de datos (organizaciones y
 * diagnósticos). Reflejan los DTO del backend Java:
 * - `OrganizacionResponse`         { UUID id, UUID tenantId, String nombre,
 *                                     String sector, String tamano,
 *                                     Instant creadoEn }
 * - `CrearOrganizacionRequest`     { @NotBlank nombre, sector,
 *                                     @Pattern(micro|pequena|mediana|grande) tamano }
 * - `DiagnosticoResumenResponse`   { id, organizacionId, organizacionNombre,
 *                                     estado, String scoreSgi, Integer nivelSgi,
 *                                     Instant creadoEn }
 * - `DiagnosticoDetalleResponse`   { id, organizacionId, organizacionNombre, estado }
 * - `CrearDiagnosticoRequest`      { @NotNull UUID organizacionId }
 * Los UUID se representan como `string`; los `Instant` como ISO `string`.
 * ------------------------------------------------------------------ */

/**
 * Tamaño de empresa aceptado por el backend en la ENTRADA
 * (`CrearOrganizacionRequest`), validado con `@Pattern`. Unión cerrada.
 */
export type OrganizacionTamano = "micro" | "pequena" | "mediana" | "grande";

/**
 * Organización tal como la devuelve el backend (`OrganizacionResponse`).
 * `tamano` se tipa `string | null` (abierto) porque el backend emite un
 * `String` libre en la salida; la unión cerrada solo aplica en la entrada
 * (mismo criterio abierto que `Rol`).
 */
export interface Organizacion {
  id: string;
  tenantId: string;
  nombre: string;
  sector: string | null;
  tamano: string | null;
  creadoEn: string;
}

/** Body de `POST /api/v1/organizaciones`. */
export interface CrearOrganizacionRequest {
  nombre: string;
  sector?: string;
  tamano?: OrganizacionTamano;
}

/**
 * Resumen de diagnóstico (`DiagnosticoResumenResponse`).
 * `scoreSgi` es STRING en el backend (no se castea a number); `nivelSgi` es
 * entero nullable; `estado` es string libre (p. ej. `"borrador"`/`"calculado"`).
 */
export interface DiagnosticoResumen {
  id: string;
  organizacionId: string;
  organizacionNombre: string;
  estado: string;
  scoreSgi: string | null;
  nivelSgi: number | null;
  creadoEn: string;
}

/** Detalle de diagnóstico (`DiagnosticoDetalleResponse`). */
export interface DiagnosticoDetalle {
  id: string;
  organizacionId: string;
  organizacionNombre: string;
  estado: string;
}

/** Body de `POST /api/v1/diagnosticos`. */
export interface CrearDiagnosticoRequest {
  organizacionId: string;
}

/* ------------------------------------------------------------------ *
 * Sub-fase 3c: tipos del contrato del cuestionario (KB + respuestas).
 * Reflejan los DTO del backend Java:
 * - `KbModuloResponse`       { String id, String nombre, @JsonProperty("short")
 *                              String short, String weight, String escala }
 * - `KbPreguntaResponse`     { String id, String pregunta, String norma,
 *                              String articulo, String requisito, String criterio,
 *                              String peso, Map<String,String> recomendacion }
 * - `GuardarRespuestasRequest` { @NotEmpty List<RespuestaItem> respuestas }
 * - `RespuestaItem`          { @NotBlank String preguntaId,
 *                              @Pattern(0|1|2|si|no|na|sin_dato) String valor }
 * - `GET  .../respuestas`    → Map<String,String> (preguntaId → valor)
 * - `PUT  .../respuestas`    → 204 No Content
 *
 * Nota: el banco KB es GLOBAL (sin tenant/RLS) pero los endpoints exigen sesión
 * válida (caen bajo `anyRequest().authenticated()`). El `short`/`weight`/`escala`
 * del módulo y `articulo`/`requisito`/`criterio`/`peso`/`recomendacion` de la
 * pregunta se tipan abiertos/opcionales porque la UI del cuestionario solo usa
 * un subconjunto (id, nombre, short / id, pregunta, norma).
 * ------------------------------------------------------------------ */

/**
 * Módulo del banco KB (`KbModuloResponse`). El backend emite la clave JSON
 * `short` (fiel a la forma original del banco); aquí se nombra `short` igual.
 */
export interface KbModulo {
  id: string;
  nombre: string;
  short: string;
  weight: string | null;
  escala: string | null;
}

/**
 * Pregunta del banco KB (`KbPreguntaResponse`). `recomendacion` es el mapa
 * `{"0":..,"1":..,"2":..}` de textos de recomendación por respuesta.
 */
export interface KbPregunta {
  id: string;
  pregunta: string;
  norma: string | null;
  articulo: string | null;
  requisito: string | null;
  criterio: string | null;
  peso: string | null;
  recomendacion: Record<string, string> | null;
}

/**
 * Valor de respuesta aceptado por el backend en la ENTRADA (`RespuestaItem`,
 * validado con `@Pattern`). Unión cerrada completa del contrato Java. La UI del
 * cuestionario (3c) solo expone el subconjunto `0 | 1 | 2 | na`.
 */
export type ValorRespuesta = "0" | "1" | "2" | "si" | "no" | "na" | "sin_dato";

/** Un item del lote de guardado (`RespuestaItem`). */
export interface RespuestaItem {
  preguntaId: string;
  valor: ValorRespuesta;
}

/** Body de `PUT /api/v1/diagnosticos/{id}/respuestas` (`GuardarRespuestasRequest`). */
export interface GuardarRespuestasRequest {
  respuestas: RespuestaItem[];
}

/**
 * Respuestas ya guardadas de un diagnóstico (`GET .../respuestas`): mapa
 * `preguntaId → valor`. Los valores vienen como string libre del backend; la UI
 * los usa tal cual para precargar el estado del cuestionario.
 */
export type RespuestasGuardadas = Record<string, string>;

/* ------------------------------------------------------------------ *
 * Sub-fase 3d: tipos del contrato de cálculo (resultado, plan de mejora,
 * benchmark). Reflejan los DTO del backend Java (`dto/calculo/*`):
 * - `ResultadoResponse`  { BigDecimal score, Integer nivelSgi,
 *                          BigDecimal cumplimientoGlobal, String organizacionNombre,
 *                          List<PilarResponse> pilares,
 *                          List<RecomendacionResponse> recomendaciones }
 * - `PilarResponse`      { String skillId, BigDecimal cumplimientoPct,
 *                          Integer nivel, String banda }
 * - `RecomendacionResponse` { String skillId, String prioridad, String texto }
 * - `TareaPlanResponse`  { String skillId, String preguntaId, String brecha,
 *                          String accion, String normaRef, String prioridad,
 *                          String responsable, Integer plazoDias, Integer fase,
 *                          String criterioCierre }
 * - `BenchmarkResponse`  (snake_case) { int nivel_empresa, double promedio_mercado,
 *                          int[] rango_mercado, int herramientas_por_encima,
 *                          int brecha_al_lider, String recomendacion }
 * - `RegistrarBenchmarkRequest` { @Min(1)@Max(5) int nivel }
 *
 * Criterios de tipado:
 * - Los `BigDecimal` (`score`, `cumplimientoGlobal`, `cumplimientoPct`) se
 *   representan como `string` (como `scoreSgi` en 3b: no se castean a number en
 *   el contrato; la UI hace `Number(...)` solo para pintar). Nullable: el
 *   backend puede emitirlos `null` si el diagnóstico aún no se calculó.
 * - El benchmark conserva las claves snake_case del JSON Java (fiel al
 *   `Benchmark.tsx` original); no se renombra a camelCase.
 * ------------------------------------------------------------------ */

/** Pilar (módulo) de un resultado (`PilarResponse`). */
export interface PilarResultado {
  skillId: string;
  cumplimientoPct: string | null;
  nivel: number | null;
  banda: string | null;
}

/** Recomendación de prioridad alta de un resultado (`RecomendacionResponse`). */
export interface RecomendacionResultado {
  skillId: string;
  prioridad: string | null;
  texto: string;
}

/**
 * Resultado consolidado de un diagnóstico (`ResultadoResponse`), devuelto por
 * `POST .../calcular` y `GET .../resultado`. `score`/`cumplimientoGlobal` como
 * string nullable; `pilares`/`recomendaciones` siempre son listas (el backend
 * emite `[]`, nunca `null`).
 */
export interface Resultado {
  score: string | null;
  nivelSgi: number | null;
  cumplimientoGlobal: string | null;
  organizacionNombre: string | null;
  pilares: PilarResultado[];
  recomendaciones: RecomendacionResultado[];
}

/** Tarea del plan de mejora (`TareaPlanResponse`). */
export interface TareaPlan {
  skillId: string;
  preguntaId: string | null;
  brecha: string | null;
  accion: string | null;
  normaRef: string | null;
  prioridad: string | null;
  responsable: string | null;
  plazoDias: number | null;
  fase: number | null;
  criterioCierre: string | null;
}

/**
 * Benchmark de mercado (`BenchmarkResponse`), en snake_case (fiel al contrato
 * Java vía `@JsonProperty`). `rango_mercado` es `[min, max]`.
 */
export interface Benchmark {
  nivel_empresa: number;
  promedio_mercado: number;
  rango_mercado: number[];
  herramientas_por_encima: number;
  brecha_al_lider: number;
  recomendacion: string;
}

/** Body de `POST /api/v1/diagnosticos/{id}/benchmark` (`RegistrarBenchmarkRequest`). */
export interface RegistrarBenchmarkRequest {
  nivel: number;
}
