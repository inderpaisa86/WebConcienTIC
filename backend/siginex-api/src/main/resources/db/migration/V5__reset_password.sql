-- Fase 5: recuperación de contraseña. La tabla siginex.reset_password ya se
-- creó en V2 (Fase 1) con (id, usuario_id, token_hash UNIQUE, expira_en, usado,
-- creado_en). Esta migración solo añade índices de apoyo para el flujo de
-- reset; NO cambia columnas ni constraints existentes.
--
-- Igual que `sesion`, reset_password se accede por token_hash / usuario_id
-- FUERA de RLS (no lleva política por tenant): la búsqueda por token ocurre
-- antes de conocer el tenant, y el borrado/invalidacion por usuario los hace la
-- lógica de aplicación bajo su propio control de acceso.

SET search_path TO siginex;

-- Búsqueda/invalidación de los tokens de un usuario (p. ej. al emitir uno nuevo
-- se invalidan los anteriores no usados, y al confirmar se marca el usado).
CREATE INDEX IF NOT EXISTS ix_reset_password_usuario
    ON siginex.reset_password (usuario_id);

-- Barrido de tokens vigentes (no usados y sin expirar). Índice parcial pequeño.
CREATE INDEX IF NOT EXISTS ix_reset_password_vigentes
    ON siginex.reset_password (expira_en)
    WHERE usado = false;
