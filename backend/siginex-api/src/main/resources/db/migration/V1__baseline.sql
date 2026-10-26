-- Fase 0 baseline migration.
-- Creates only the SIGINEX schema. NO business tables here.
-- Business tables (usuario, sesion, reset_password, etc.) and their RLS
-- policies arrive in later phases (Fase 1+).
CREATE SCHEMA IF NOT EXISTS siginex;
