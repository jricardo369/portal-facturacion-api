package com.portalfacturacion.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record TicketResponse(String numeroTicket, BigDecimal total, BigDecimal subtotal,
                             BigDecimal impuestos, String estado, String tipoVenta,
                             Instant fechaEmision, Instant fechaCierre, String cliente,
                             String mesa, String sucursal, boolean facturable,
                             List<TicketItemResponse> items, List<TicketPagoResponse> pagos) {
}
