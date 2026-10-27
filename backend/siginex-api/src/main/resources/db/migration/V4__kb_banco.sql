-- Fase 2c: tablas del BANCO DE CONOCIMIENTO (KB) del diagnóstico SIGINEX.
-- El banco es GLOBAL y COMPARTIDO entre todos los tenants, igual que
-- kb_version (creada en V3). Por eso estas tablas NO llevan tenant_id NI RLS:
-- lo por-tenant son las RESPUESTAS (tabla siginex.respuesta, ya en V3).
--
-- El contenido NO se inserta aquí con INSERTs gigantes: lo carga en arranque un
-- seed idempotente por código (KbSeedService/KbSeedRunner) que lee
-- classpath:kb/banco.json y hace upsert respetando kb_version. Esta migración
-- sólo crea la estructura.

SET search_path TO siginex;

-- kb_modulo: un módulo del banco (sst, ambiental, calidad, ...). GLOBAL, sin RLS.
-- weight es el peso del módulo en el score global; escala puede ser NULL (los
-- módulos esg y gobernanza_ia traen "" en el JSON, que el seed normaliza a NULL).
-- orden preserva el orden de los módulos tal como aparecen en banco.json.
CREATE TABLE siginex.kb_modulo (
    id          text PRIMARY KEY,
    kb_version  text NOT NULL REFERENCES siginex.kb_version(version),
    nombre      text NOT NULL,
    short       text,
    weight      numeric,
    escala      text,
    orden       integer NOT NULL
);
CREATE INDEX ix_kb_modulo_version ON siginex.kb_modulo (kb_version);

-- kb_pregunta: una pregunta de un módulo. GLOBAL, sin RLS. recomendacion es el
-- objeto {"0":..,"1":..,"2":..} del JSON almacenado como jsonb. orden preserva
-- el orden de las preguntas dentro del módulo tal como aparecen en banco.json.
CREATE TABLE siginex.kb_pregunta (
    id             text PRIMARY KEY,
    modulo_id      text NOT NULL REFERENCES siginex.kb_modulo(id),
    kb_version     text NOT NULL,
    pregunta       text NOT NULL,
    norma          text,
    articulo       text,
    requisito      text,
    criterio       text,
    peso           numeric,
    recomendacion  jsonb NOT NULL DEFAULT '{}'::jsonb,
    orden          integer NOT NULL
);
CREATE INDEX ix_kb_pregunta_modulo  ON siginex.kb_pregunta (modulo_id);
CREATE INDEX ix_kb_pregunta_version ON siginex.kb_pregunta (kb_version);
