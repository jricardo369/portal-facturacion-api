package com.portalfacturacion.application.port.in.cliente;

import com.portalfacturacion.domain.model.Cliente;

public interface CrearClienteUseCase {
  Cliente crear(Cliente cliente);
}
