package com.portalfacturacion.adapter.in.web.response;

import java.math.BigDecimal;

public record TicketPagoResponse(String id, String metodo, BigDecimal monto) {
}
