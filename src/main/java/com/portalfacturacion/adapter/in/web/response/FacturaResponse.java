package com.portalfacturacion.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.Instant;

public record FacturaResponse(Long idFactura, String folio, String serie, Instant fecha,
                              String cliente, String rfc, String razonSocial, String noTicket,
                              BigDecimal subtotal, BigDecimal impuesto, BigDecimal total,
                              String estatus, String uuid, Instant createdAt, Instant updatedAt) {
}
