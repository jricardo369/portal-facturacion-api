package com.portalfacturacion.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarConfiguracionRequest(
    @NotBlank @Size(max = 255) String valor,
    @Size(max = 255) String descripcion,
    Boolean activo) {
}
