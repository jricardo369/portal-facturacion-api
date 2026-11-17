package com.portalfacturacion.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record FacturaResponse(Long idFactura, String folio, String serie, Instant fecha,
                               String cliente, String rfc, String razonSocial, String noTicket,
                               BigDecimal subtotal, BigDecimal impuesto, BigDecimal iva, BigDecimal isr,
                               BigDecimal cedular, BigDecimal total,
                               String estatus, String uuid, String facturapiId,
                                String cancellationStatus, Instant createdAt, Instant updatedAt, List<TicketFacturaResponse> tickets) {
}
