package com.siginex.api.dto;

/**
 * Minimal read-only status DTO for the Fase 0 skeleton.
 *
 * <p>It is a {@code record} kept separate from any future domain entity and
 * touches no database. Later phases add the real layered API DTOs.
 */
public record StatusResponse(String status, String application) {
}
