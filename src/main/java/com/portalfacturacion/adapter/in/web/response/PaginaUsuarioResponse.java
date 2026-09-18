package com.portalfacturacion.adapter.in.web.response;

import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.domain.model.Usuario;
import java.util.List;

public record PaginaUsuarioResponse(List<UsuarioResponse> content, int page, int size,
                                    long totalElements, int totalPages) {
  public static PaginaUsuarioResponse from(PagedResult<Usuario> page, java.util.function.Function<Usuario, UsuarioResponse> mapper) {
    return new PaginaUsuarioResponse(page.content().stream().map(mapper).toList(),
        page.page(), page.size(), page.totalElements(), page.totalPages());
  }
}
