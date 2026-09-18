package com.portalfacturacion.application.port.in.cliente;

import com.portalfacturacion.domain.model.Cliente;

public interface ActualizarClienteUseCase {
  Cliente actualizarPorRfc(String rfc, Cliente cambios);
}
