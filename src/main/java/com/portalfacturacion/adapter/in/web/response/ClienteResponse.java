package com.portalfacturacion.adapter.in.web.response;

import java.time.Instant;

public record ClienteResponse(Long idCliente, String rfc, String razonSocial, String calle,
                              String numExterior, String numInterior, String referencia,
                              String estado, String municipio, String colonia,
                              String codigoPostal, String correoElectronico,
                              String regimenFiscal, String usoFactura, Boolean estatus,
                              Instant createdAt, Instant updatedAt) {
}
