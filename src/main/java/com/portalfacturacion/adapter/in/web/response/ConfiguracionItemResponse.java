package com.portalfacturacion.adapter.in.web.response;

import com.portalfacturacion.domain.model.Configuracion;
import java.time.Instant;

public record ConfiguracionItemResponse(Long id, String clave, String valor, String descripcion,
    Boolean activo, Instant createdAt, Instant updatedAt) {

  public static ConfiguracionItemResponse from(Configuracion c) {
    return new ConfiguracionItemResponse(c.id(), c.clave(), c.valor(), c.descripcion(),
        c.activo(), c.createdAt(), c.updatedAt());
  }
}
