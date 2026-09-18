package com.portalfacturacion.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "usuario es obligatorio") String usuario,
    @NotBlank(message = "password es obligatorio") String password) {
}
