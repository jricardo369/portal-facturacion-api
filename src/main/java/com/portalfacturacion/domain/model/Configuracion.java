package com.portalfacturacion.domain.model;

public record Configuracion(Long id, String clave, String valor, String descripcion,
    Boolean activo, java.time.Instant createdAt, java.time.Instant updatedAt) {
}

