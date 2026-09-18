package com.portalfacturacion.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record DatosFacturaResponse(String numeroTicket, BigDecimal total, BigDecimal subtotal,
                                   BigDecimal impuestos, String tipoVenta,
                                   Instant fechaEmision, Instant fechaCierre,
                                   String clienteTicket, boolean facturable,
                                   List<TicketItemResponse> items,
                                   List<TicketPagoResponse> pagos,
                                   String tipoPago,
                                   ClienteResponse cliente) {
}
