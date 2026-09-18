package com.portalfacturacion.application.port.in.cliente;

import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.domain.model.Cliente;

public interface ConsultarClienteUseCase {
  Cliente obtenerPorRfc(String rfc);
  boolean existePorRfc(String rfc);
  PagedResult<Cliente> listar(String rfc, String razonSocial, String codigoPostal, int page, int size, String sort);
}
