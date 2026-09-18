package com.portalfacturacion.application.port.out.persistence;

import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.domain.model.Cliente;
import java.util.Optional;

public interface ClienteRepositoryPort {
  Cliente save(Cliente cliente);
  Optional<Cliente> findByRfc(String rfc);
  boolean existsByRfc(String rfc);
  PagedResult<Cliente> findActivos(String rfc, String razonSocial, String codigoPostal, int page, int size, String sort);
}
