-- Fase 1: autenticación y usuarios. Schema siginex ya creado en V1.
-- Aislamiento por tenant con RLS fiel al SIGINEX original (drizzle/0000_init.sql).

SET search_path TO siginex;

-- current_tenant(): lee el setting app.tenant_id fijado por transacción.
CREATE OR REPLACE FUNCTION siginex.current_tenant() RETURNS uuid
LANGUAGE sql STABLE AS $$
  SELECT NULLIF(current_setting('app.tenant_id', true), '')::uuid
$$;

-- tenant: raíz del aislamiento, SIN RLS.
CREATE TABLE IF NOT EXISTS siginex.tenant (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre     text NOT NULL,
    slug       text NOT NULL,
    activo     boolean NOT NULL DEFAULT true,
    creado_en  timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT tenant_slug_unique UNIQUE (slug)
);

-- usuario: CON RLS por tenant_id. email único global.
CREATE TABLE siginex.usuario (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        uuid NOT NULL REFERENCES siginex.tenant(id) ON DELETE CASCADE,
    email            text NOT NULL,
    nombre           text,
    password_hash    text NOT NULL,
    rol              text NOT NULL DEFAULT 'miembro',
    activo           boolean NOT NULL DEFAULT true,
    fallos_login     smallint NOT NULL DEFAULT 0,
    bloqueado_hasta  timestamptz,
    creado_en        timestamptz NOT NULL DEFAULT now(),
    ultimo_login     timestamptz,
    CONSTRAINT usuario_rol_check CHECK (rol IN ('admin','miembro'))
);
CREATE UNIQUE INDEX ux_usuario_email ON siginex.usuario (lower(email));
CREATE INDEX ix_usuario_tenant ON siginex.usuario (tenant_id);

-- sesion: token nunca en claro, sólo su hash. Revocable.
CREATE TABLE siginex.sesion (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id  uuid NOT NULL REFERENCES siginex.usuario(id) ON DELETE CASCADE,
    tenant_id   uuid NOT NULL REFERENCES siginex.tenant(id) ON DELETE CASCADE,
    token_hash  text NOT NULL,
    expira_en   timestamptz NOT NULL,
    creado_en   timestamptz NOT NULL DEFAULT now(),
    user_agent  text,
    ip          inet,
    revocada    boolean NOT NULL DEFAULT false,
    CONSTRAINT sesion_token_hash_unique UNIQUE (token_hash)
);
CREATE INDEX ix_sesion_usuario ON siginex.sesion (usuario_id);

-- reset_password: Fase 5. Tabla creada ahora, SIN endpoints.
CREATE TABLE siginex.reset_password (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id  uuid NOT NULL REFERENCES siginex.usuario(id) ON DELETE CASCADE,
    token_hash  text NOT NULL,
    expira_en   timestamptz NOT NULL,
    usado       boolean NOT NULL DEFAULT false,
    creado_en   timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT reset_password_token_hash_unique UNIQUE (token_hash)
);

-- evento_auditoria: réplica mínima del original (login/logout/accesos).
CREATE TABLE IF NOT EXISTS siginex.evento_auditoria (
    id         bigserial PRIMARY KEY,
    tenant_id  uuid,
    actor      text,
    accion     text NOT NULL,
    entidad    text,
    entidad_id text,
    payload    jsonb,
    creado_en  timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_evt_tenant
    ON siginex.evento_auditoria (tenant_id, creado_en DESC NULLS LAST);

-- RLS sobre usuario (tenant es raíz, sin RLS; sesion/reset se acceden por
-- token_hash/usuario_id fuera de RLS y van filtradas por la lógica de app).
ALTER TABLE siginex.usuario ENABLE ROW LEVEL SECURITY;
ALTER TABLE siginex.usuario FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON siginex.usuario
    USING (tenant_id = siginex.current_tenant())
    WITH CHECK (tenant_id = siginex.current_tenant());

-- ALTERNATIVA (sólo si la cuenta de la app NO tiene BYPASSRLS): búsqueda de
-- login por email mediante función SECURITY DEFINER acotada. Comentada por
-- defecto; ver decisión D4.
-- CREATE OR REPLACE FUNCTION siginex.buscar_usuario_para_login(p_email text)
-- RETURNS TABLE (id uuid, tenant_id uuid, password_hash text, activo boolean,
--                rol text, fallos_login smallint, bloqueado_hasta timestamptz)
-- LANGUAGE sql SECURITY DEFINER SET search_path = siginex AS $$
--   SELECT id, tenant_id, password_hash, activo, rol, fallos_login, bloqueado_hasta
--   FROM siginex.usuario WHERE lower(email) = lower(p_email)
-- $$;
