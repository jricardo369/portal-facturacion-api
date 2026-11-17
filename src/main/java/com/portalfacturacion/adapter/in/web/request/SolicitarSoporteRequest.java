package com.portalfacturacion.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SolicitarSoporteRequest(
    @NotBlank @Size(max = 150) String nombre,
    @NotBlank @Size(max = 32) String motivo,
    @Size(max = 40) String fecha,
    @Size(max = 20) String hora,
    @Size(max = 20) String total,
    @NotBlank @Size(max = 2000) String descripcion) {
}
