package com.portalfacturacion.adapter.in.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnviarFacturaRequest(
    @NotBlank @Email @Size(max = 150) String correoElectronico,
    @NotBlank @Pattern(regexp = "^[A-Za-z0-9\\-]{1,30}$", message = "Formato de ticket invalido")
    String numeroTicket) {
}
