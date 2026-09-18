package com.portalfacturacion.adapter.in.web.response;

import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.domain.model.Factura;
import java.util.List;
import java.util.function.Function;

public record PaginaFacturaResponse(List<FacturaResponse> content, int page, int size,
                                       long totalElements, int totalPages) {
  public static PaginaFacturaResponse from(PagedResult<Factura> page,
      Function<Factura, FacturaResponse> mapper) {
    return new PaginaFacturaResponse(page.content().stream().map(mapper).toList(),
        page.page(), page.size(), page.totalElements(), page.totalPages());
  }
}
