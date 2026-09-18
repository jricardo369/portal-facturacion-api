package com.portalfacturacion.application.service;

import com.portalfacturacion.application.port.in.cliente.ActualizarClienteUseCase;
import com.portalfacturacion.application.port.in.cliente.ConsultarClienteUseCase;
import com.portalfacturacion.application.port.in.cliente.CrearClienteUseCase;
import com.portalfacturacion.application.port.in.cliente.EliminarClienteUseCase;
import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.application.port.out.persistence.ClienteRepositoryPort;
import com.portalfacturacion.domain.exception.ClienteDuplicadoException;
import com.portalfacturacion.domain.exception.ClienteNoEncontradoException;
import com.portalfacturacion.domain.model.Cliente;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService implements CrearClienteUseCase, ConsultarClienteUseCase,
    ActualizarClienteUseCase, EliminarClienteUseCase {

  private final ClienteRepositoryPort repository;

  public ClienteService(ClienteRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public Cliente crear(Cliente cliente) {
    String rfc = normalizarRfc(cliente.getRfc());
    cliente.setRfc(rfc);
    if (repository.existsByRfc(rfc)) {
      throw new ClienteDuplicadoException("Ya existe un cliente con RFC '" + rfc + "'");
    }
    cliente.setIdCliente(null);
    cliente.setEstatus(true);
    cliente.setCreatedAt(Instant.now());
    cliente.setUpdatedAt(Instant.now());
    return repository.save(cliente);
  }

  @Override
  @Transactional(readOnly = true)
  public Cliente obtenerPorRfc(String rfc) {
    return repository.findByRfc(normalizarRfc(rfc))
        .filter(c -> Boolean.TRUE.equals(c.getEstatus()))
        .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado con RFC " + rfc));
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existePorRfc(String rfc) {
    return repository.existsByRfc(normalizarRfc(rfc));
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResult<Cliente> listar(String rfc, String razonSocial, String codigoPostal, int page, int size, String sort) {
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 100);
    return repository.findActivos(rfc, razonSocial, codigoPostal, safePage, safeSize, sort);
  }

  @Override
  @Transactional
  public Cliente actualizarPorRfc(String rfc, Cliente cambios) {
    Cliente actual = obtenerPorRfc(rfc);
    actual.setRazonSocial(cambios.getRazonSocial());
    actual.setCalle(cambios.getCalle());
    actual.setNumExterior(cambios.getNumExterior());
    actual.setNumInterior(cambios.getNumInterior());
    actual.setReferencia(cambios.getReferencia());
    actual.setEstado(cambios.getEstado());
    actual.setMunicipio(cambios.getMunicipio());
    actual.setColonia(cambios.getColonia());
    actual.setCodigoPostal(cambios.getCodigoPostal());
    actual.setCorreoElectronico(cambios.getCorreoElectronico());
    actual.setRegimenFiscal(cambios.getRegimenFiscal());
    actual.setUsoFactura(cambios.getUsoFactura());
    actual.setUpdatedAt(Instant.now());
    return repository.save(actual);
  }

  @Override
  @Transactional
  public void eliminarPorRfc(String rfc) {
    Cliente actual = obtenerPorRfc(rfc);
    actual.setEstatus(false);
    actual.setUpdatedAt(Instant.now());
    repository.save(actual);
  }

  private String normalizarRfc(String rfc) {
    if (rfc == null) {
      throw new IllegalArgumentException("El RFC es obligatorio");
    }
    return rfc.trim().toUpperCase();
  }
}
