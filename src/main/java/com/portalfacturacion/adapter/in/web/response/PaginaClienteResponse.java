package com.portalfacturacion.adapter.in.web.response;

import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.domain.model.Cliente;
import java.util.List;
import java.util.function.Function;

public record PaginaClienteResponse(List<ClienteResponse> content, int page, int size,
                                    long totalElements, int totalPages) {
  public static PaginaClienteResponse from(PagedResult<Cliente> page, Function<Cliente, ClienteResponse> mapper) {
    return new PaginaClienteResponse(page.content().stream().map(mapper).toList(),
        page.page(), page.size(), page.totalElements(), page.totalPages());
  }
}
