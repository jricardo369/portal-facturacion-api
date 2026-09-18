package com.portalfacturacion.adapter.in.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ActualizarClienteRequest(
    @NotBlank @Size(max = 255) String razonSocial,
    @Size(max = 150) String calle,
    @Size(max = 20) String numExterior,
    @Size(max = 20) String numInterior,
    @Size(max = 255) String referencia,
    @Size(max = 100) String estado,
    @Size(max = 100) String municipio,
    @Size(max = 100) String colonia,
    @Pattern(regexp = "^\\d{5}$", message = "El codigo postal debe tener 5 digitos")
    String codigoPostal,
    @Email @Size(max = 150) String correoElectronico,
    @Size(max = 10) String regimenFiscal,
    @Size(max = 10) String usoFactura) {
}
