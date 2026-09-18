package com.portalfacturacion.adapter.in.web.response;

import java.math.BigDecimal;

public record TicketItemResponse(String id, String descripcion, BigDecimal cantidad,
                                 BigDecimal precioUnitario, BigDecimal importe) {
}
