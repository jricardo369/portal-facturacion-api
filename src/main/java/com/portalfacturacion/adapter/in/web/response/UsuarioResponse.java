package com.portalfacturacion.adapter.in.web.response;

import com.portalfacturacion.domain.model.TipoUsuario;
import java.time.Instant;

public record UsuarioResponse(Long id, String usuario, String nombre, String direccion,
                              String telefono, TipoUsuario tipo, Boolean activo,
                              Instant createdAt, Instant updatedAt) {
}
