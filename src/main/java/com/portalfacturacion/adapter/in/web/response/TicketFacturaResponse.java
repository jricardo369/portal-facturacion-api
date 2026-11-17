package com.portalfacturacion.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.Instant;

public record TicketFacturaResponse(Long id, String numeroTicket, Instant fechaEmision, String hora,
                                   BigDecimal subtotal, BigDecimal iva, BigDecimal isr,
                                   BigDecimal cedular, BigDecimal total) {
}
