-- Fase 2a: esquema de negocio del diagnóstico SIGINEX (tablas, enums, checks,
-- índices y RLS por tenant). Fiel a drizzle/0000_init.sql del SIGINEX original.
-- V1 creó el schema; V2 creó current_tenant(), tenant, usuario, sesion,
-- reset_password y evento_auditoria. Aquí NO se recrea nada de eso.

SET search_path TO siginex;

-- Extensiones: pg_trgm es necesaria para el índice GIN trigram ix_org_nombre_trgm
-- sobre organizacion.nombre. NO se crea pgcrypto: el original la usaba sólo para
-- gen_random_uuid(), que es core de PostgreSQL desde la v13 (contenedor pg17),
-- igual que ya asume V2.
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- =====================================================================
-- Enums de negocio (ninguno existe aún: V1/V2 no crean enums). Sin
-- IF NOT EXISTS: deben fallar ruidosamente si se duplicaran.
-- =====================================================================
CREATE TYPE siginex.estado_diag       AS ENUM ('en_progreso', 'calculado', 'archivado');
CREATE TYPE siginex.estado_tarea      AS ENUM ('pendiente', 'en_progreso', 'cerrada', 'vencida');
CREATE TYPE siginex.modo_diagnostico  AS ENUM ('completo', 'rapido');
CREATE TYPE siginex.nivel_tecnologico AS ENUM ('1', '2', '3', '4', '5');
CREATE TYPE siginex.origen_ruta       AS ENUM ('interno', 'externo');
CREATE TYPE siginex.prioridad_reco    AS ENUM ('alta', 'media', 'baja');
CREATE TYPE siginex.tamano_empresa    AS ENUM ('micro', 'pequena', 'mediana', 'grande');
CREATE TYPE siginex.tipo_pregunta     AS ENUM ('si_no', 'escala', 'cero_uno_dos');
CREATE TYPE siginex.valor_respuesta   AS ENUM ('0', '1', '2', 'si', 'no', 'na', 'sin_dato');

-- =====================================================================
-- Tablas globales (SIN RLS, SIN tenant_id): banco de KB y feeds de vigilancia.
-- =====================================================================

-- kb_version: banco de referencia de preguntas. GLOBAL, sin RLS.
CREATE TABLE siginex.kb_version (
    version          text PRIMARY KEY,
    publicado_en     timestamptz NOT NULL DEFAULT now(),
    checksum         text,
    total_preguntas  integer,
    notas            text
);

-- normativa_entry: feed de vigilancia normativa. GLOBAL, sin RLS.
CREATE TABLE siginex.normativa_entry (
    id           text PRIMARY KEY,
    modulo_sgi   text,
    fuente       text,
    tipo         text,
    titulo       text NOT NULL,
    fecha        date,
    url          text,
    estado       text,
    relevancia   text,
    resumen      text,
    ingerido_en  timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_norm_modulo ON siginex.normativa_entry (modulo_sgi, estado);

-- market_signal: feed de señales de mercado. GLOBAL, sin RLS.
CREATE TABLE siginex.market_signal (
    id           text PRIMARY KEY,
    tipo         text,
    titulo       text NOT NULL,
    fecha        date,
    impacto      text,
    fuente       text,
    resumen      text,
    ingerido_en  timestamptz NOT NULL DEFAULT now()
);

-- =====================================================================
-- Tablas de negocio con tenant (RLS aplicada más abajo en bloque).
-- =====================================================================

-- organizacion: empresa diagnosticada. CON RLS por tenant_id.
CREATE TABLE siginex.organizacion (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      uuid NOT NULL REFERENCES siginex.tenant(id) ON DELETE CASCADE,
    nombre         text NOT NULL,
    nit            text,
    sector         text,
    tamano         siginex.tamano_empresa,
    aplicabilidad  jsonb NOT NULL DEFAULT '{}'::jsonb,
    creado_en      timestamptz NOT NULL DEFAULT now(),
    actualizado_en timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_org_tenant     ON siginex.organizacion (tenant_id);
CREATE INDEX ix_org_sector     ON siginex.organizacion (sector);
CREATE INDEX ix_org_nombre_trgm ON siginex.organizacion USING gin (nombre gin_trgm_ops);

-- diagnostico: cabecera del diagnóstico SGI. CON RLS por tenant_id.
CREATE TABLE siginex.diagnostico (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           uuid NOT NULL REFERENCES siginex.tenant(id) ON DELETE CASCADE,
    organizacion_id     uuid NOT NULL REFERENCES siginex.organizacion(id) ON DELETE CASCADE,
    kb_version          text NOT NULL REFERENCES siginex.kb_version(version),
    estado              siginex.estado_diag NOT NULL DEFAULT 'en_progreso',
    modo                siginex.modo_diagnostico NOT NULL DEFAULT 'completo',
    exigir_completitud  boolean NOT NULL DEFAULT false,
    score_sgi           numeric(5, 2),
    nivel_sgi           smallint,
    cumplimiento_global numeric(5, 2),
    madurez_global      numeric(4, 2),
    nivel_tecnologico   siginex.nivel_tecnologico,
    creado_en           timestamptz NOT NULL DEFAULT now(),
    completado_en       timestamptz,
    idempotency_key     text,
    CONSTRAINT diagnostico_nivel_sgi_check CHECK (nivel_sgi BETWEEN 1 AND 5)
);
CREATE INDEX ix_diag_tenant ON siginex.diagnostico (tenant_id);
CREATE INDEX ix_diag_org     ON siginex.diagnostico (organizacion_id, creado_en DESC NULLS LAST);
CREATE UNIQUE INDEX ux_diag_idem ON siginex.diagnostico (tenant_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;

-- evidencia y respuesta tienen dependencia circular (respuesta.evidencia_ref ->
-- evidencia.id y evidencia.respuesta_id -> respuesta.id). Se crean ambas SIN la
-- FK cruzada inline y las dos FKs se añaden por ALTER TABLE tras crear ambas,
-- igual que el original (drizzle/0000_init.sql).

-- evidencia: adjunto de una respuesta. CON RLS por tenant_id.
CREATE TABLE siginex.evidencia (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    uuid NOT NULL REFERENCES siginex.tenant(id) ON DELETE CASCADE,
    respuesta_id uuid NOT NULL,  -- FK a respuesta añadida más abajo (circular)
    tipo         text NOT NULL,
    uri          text,
    hash         text,
    creado_en    timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT evidencia_tipo_check CHECK (tipo IN ('url', 'archivo', 'nota'))
);
CREATE INDEX ix_evid_resp ON siginex.evidencia (respuesta_id);

-- respuesta: respuesta a una pregunta del diagnóstico. CON RLS por tenant_id.
CREATE TABLE siginex.respuesta (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       uuid NOT NULL REFERENCES siginex.tenant(id) ON DELETE CASCADE,
    diagnostico_id  uuid NOT NULL REFERENCES siginex.diagnostico(id) ON DELETE CASCADE,
    pregunta_id     text NOT NULL,
    skill_id        text NOT NULL,
    tipo            siginex.tipo_pregunta NOT NULL,
    valor           siginex.valor_respuesta,
    peso            numeric(8, 5),
    evidencia_ref   uuid,  -- FK a evidencia añadida más abajo (circular)
    actualizado_en  timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT respuesta_diagnostico_pregunta_key UNIQUE (diagnostico_id, pregunta_id)
);
CREATE INDEX ix_resp_diag  ON siginex.respuesta (diagnostico_id);
CREATE INDEX ix_resp_skill ON siginex.respuesta (diagnostico_id, skill_id);

-- resultado_pilar: resultado agregado por pilar/skill. CON RLS por tenant_id.
CREATE TABLE siginex.resultado_pilar (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        uuid NOT NULL REFERENCES siginex.tenant(id) ON DELETE CASCADE,
    diagnostico_id   uuid NOT NULL REFERENCES siginex.diagnostico(id) ON DELETE CASCADE,
    skill_id         text NOT NULL,
    score            numeric(5, 2),
    nivel            smallint,
    cumplimiento_pct numeric(5, 2),
    madurez_promedio numeric(4, 2),
    banda            text,
    hallazgos        jsonb NOT NULL DEFAULT '[]'::jsonb,
    CONSTRAINT resultado_pilar_diagnostico_skill_key UNIQUE (diagnostico_id, skill_id),
    CONSTRAINT resultado_pilar_nivel_check CHECK (nivel BETWEEN 1 AND 5)
);
CREATE INDEX ix_respil_diag ON siginex.resultado_pilar (diagnostico_id);

-- recomendacion: recomendación derivada del diagnóstico. CON RLS por tenant_id.
CREATE TABLE siginex.recomendacion (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      uuid NOT NULL REFERENCES siginex.tenant(id) ON DELETE CASCADE,
    diagnostico_id uuid NOT NULL REFERENCES siginex.diagnostico(id) ON DELETE CASCADE,
    skill_id       text NOT NULL,
    pregunta_id    text,
    prioridad      siginex.prioridad_reco NOT NULL,
    score          numeric(5, 2),
    texto          text NOT NULL,
    acciones       jsonb NOT NULL DEFAULT '[]'::jsonb,
    norma_refs     jsonb NOT NULL DEFAULT '[]'::jsonb,
    creado_en      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_reco_diag ON siginex.recomendacion (diagnostico_id, prioridad);

-- ruta_aprendizaje: recurso de aprendizaje para cerrar brechas. CON RLS.
CREATE TABLE siginex.ruta_aprendizaje (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       uuid NOT NULL REFERENCES siginex.tenant(id) ON DELETE CASCADE,
    recomendacion_id uuid REFERENCES siginex.recomendacion(id) ON DELETE CASCADE,
    diagnostico_id  uuid NOT NULL REFERENCES siginex.diagnostico(id) ON DELETE CASCADE,
    skill_id        text NOT NULL,
    brecha          text NOT NULL,
    recurso_url     text,
    origen          siginex.origen_ruta NOT NULL DEFAULT 'interno',
    creado_en       timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_ruta_diag ON siginex.ruta_aprendizaje (diagnostico_id);

-- tarea_mejora: tarea del plan de mejora. CON RLS por tenant_id.
CREATE TABLE siginex.tarea_mejora (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id          uuid NOT NULL REFERENCES siginex.tenant(id) ON DELETE CASCADE,
    diagnostico_id     uuid NOT NULL REFERENCES siginex.diagnostico(id) ON DELETE CASCADE,
    recomendacion_id   uuid REFERENCES siginex.recomendacion(id) ON DELETE SET NULL,
    skill_id           text NOT NULL,
    pregunta_id        text,
    brecha             text NOT NULL,
    accion             text NOT NULL,
    norma_ref          text,
    prioridad          siginex.prioridad_reco NOT NULL,
    responsable        text,
    plazo_dias         integer,
    fecha_limite       date,
    esfuerzo           text,
    fase               smallint,
    criterio_cierre    text,
    estado             siginex.estado_tarea NOT NULL DEFAULT 'pendiente',
    ruta_aprendizaje_id uuid REFERENCES siginex.ruta_aprendizaje(id) ON DELETE SET NULL,
    creado_en          timestamptz NOT NULL DEFAULT now(),
    cerrado_en         timestamptz,
    CONSTRAINT tarea_mejora_esfuerzo_check CHECK (esfuerzo IN ('Alto', 'Medio', 'Bajo')),
    CONSTRAINT tarea_mejora_fase_check CHECK (fase BETWEEN 1 AND 3)
);
CREATE INDEX ix_tarea_diag   ON siginex.tarea_mejora (diagnostico_id, prioridad);
CREATE INDEX ix_tarea_estado ON siginex.tarea_mejora (estado, fecha_limite);

-- alerta: SIN RLS y con tenant_id NULLABLE, fiel al original
-- (drizzle/0000_init.sql NO incluye alerta en el bloque RLS). Al no llevar RLS,
-- un rol aplicativo ve todas las filas, incluidas las de tenant_id NULL
-- ("alertas globales"); la segmentación por tenant se gestiona por otra vía.
CREATE TABLE siginex.alerta (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    uuid REFERENCES siginex.tenant(id) ON DELETE CASCADE,
    tipo         text NOT NULL,
    severidad    text NOT NULL,
    titulo       text NOT NULL,
    detalle      jsonb NOT NULL DEFAULT '{}'::jsonb,
    origen_id    text,
    estado       text NOT NULL DEFAULT 'nueva',
    creado_en    timestamptz NOT NULL DEFAULT now(),
    despachada_en timestamptz,
    CONSTRAINT alerta_tipo_check      CHECK (tipo IN ('normativa', 'mercado', 'plan')),
    CONSTRAINT alerta_severidad_check CHECK (severidad IN ('critica', 'alta', 'media', 'informativa')),
    CONSTRAINT alerta_estado_check    CHECK (estado IN ('nueva', 'despachada', 'resuelta'))
);
CREATE INDEX ix_alerta_estado ON siginex.alerta (estado, severidad, creado_en DESC NULLS LAST);

-- =====================================================================
-- FKs circulares respuesta <-> evidencia (aplicadas aquí, tras crear ambas),
-- replicando el enfoque del original (drizzle/0000_init.sql).
-- =====================================================================
ALTER TABLE siginex.evidencia ADD CONSTRAINT evidencia_respuesta_id_fkey
    FOREIGN KEY (respuesta_id) REFERENCES siginex.respuesta(id) ON DELETE CASCADE;
ALTER TABLE siginex.respuesta ADD CONSTRAINT fk_resp_evid
    FOREIGN KEY (evidencia_ref) REFERENCES siginex.evidencia(id) ON DELETE SET NULL;

-- =====================================================================
-- Row-Level Security (aislamiento por tenant). Mecánica EXACTA de V2 y del
-- original: ENABLE + FORCE ROW LEVEL SECURITY + policy tenant_isolation con
-- USING y WITH CHECK (tenant_id = siginex.current_tenant()). La función
-- current_tenant() ya existe (V2), NO se redefine aquí. La app fija el tenant
-- por transacción con SELECT set_config('app.tenant_id', '<uuid>', true).
-- Fiel al original (drizzle/0000_init.sql): RLS se aplica EXACTAMENTE a estas 8
-- tablas. NO llevan RLS las tablas globales (kb_version, normativa_entry,
-- market_signal) ni 'alerta' (el original la deja sin RLS; su tenant_id es
-- nullable y se segmenta por otra vía).
-- =====================================================================
DO $$
DECLARE t text;
BEGIN
  FOREACH t IN ARRAY ARRAY[
    'organizacion','diagnostico','respuesta','evidencia',
    'resultado_pilar','recomendacion','ruta_aprendizaje','tarea_mejora'
  ] LOOP
    EXECUTE format('ALTER TABLE siginex.%I ENABLE ROW LEVEL SECURITY;', t);
    EXECUTE format('ALTER TABLE siginex.%I FORCE ROW LEVEL SECURITY;', t);
    EXECUTE format($f$
      CREATE POLICY tenant_isolation ON siginex.%I
      USING (tenant_id = siginex.current_tenant())
      WITH CHECK (tenant_id = siginex.current_tenant());
    $f$, t);
  END LOOP;
END $$;

-- =====================================================================
-- Semilla de versión de KB (banco de referencia; ajustar checksum en
-- despliegue). Permite que diagnostico.kb_version tenga una versión válida.
-- =====================================================================
INSERT INTO siginex.kb_version (version, total_preguntas, notas)
VALUES ('3.0.0', 521, 'Banco de referencia (artefacto "500"); SST=60 oficiales Res. 0312')
ON CONFLICT (version) DO NOTHING;
