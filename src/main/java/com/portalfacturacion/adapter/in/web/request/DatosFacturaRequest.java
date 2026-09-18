package com.portalfacturacion.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DatosFacturaRequest(
    @Pattern(regexp = "^$|^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$", message = "Formato de RFC invalido")
    String rfc,
    @NotBlank @Pattern(regexp = "^[A-Za-z0-9\\-]{1,30}$", message = "Formato de ticket invalido")
    String numeroTicket) {
}
