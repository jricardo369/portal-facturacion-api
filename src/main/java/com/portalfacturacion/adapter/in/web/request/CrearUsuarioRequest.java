package com.portalfacturacion.adapter.in.web.request;

import com.portalfacturacion.domain.model.TipoUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearUsuarioRequest(
    @NotBlank @Size(min = 3, max = 64) String usuario,
    @NotBlank @Size(min = 8, max = 100) String password,
    @Size(max = 150) String nombre,
    @Size(max = 255) String direccion,
    @Size(max = 32) String telefono,
    TipoUsuario tipo) {
}
