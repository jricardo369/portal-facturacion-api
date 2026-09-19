package com.portalfacturacion.application.service;

import com.portalfacturacion.application.port.in.factura.ActualizarFacturaUseCase;
import com.portalfacturacion.application.port.in.factura.ConsultarFacturaUseCase;
import com.portalfacturacion.application.port.in.factura.CrearFacturaUseCase;
import com.portalfacturacion.application.port.in.factura.EliminarFacturaUseCase;
import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.application.port.out.persistence.FacturaRepositoryPort;
import com.portalfacturacion.domain.exception.FacturaDuplicadaException;
import com.portalfacturacion.domain.exception.FacturaNoEncontradaException;
import com.portalfacturacion.domain.model.Factura;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FacturaService implements CrearFacturaUseCase, ConsultarFacturaUseCase,
    ActualizarFacturaUseCase, EliminarFacturaUseCase {

  private final FacturaRepositoryPort repository;

  public FacturaService(FacturaRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public Factura crear(Factura factura) {
    validarObligatorios(factura);
    String serie = normalizarSerie(factura.getSerie());
    String folio = factura.getFolio().trim();
    factura.setSerie(serie);
    factura.setFolio(folio);
    factura.setRfc(factura.getRfc().trim().toUpperCase());
    if (repository.existsBySerieAndFolio(serie, folio)) {
      throw new FacturaDuplicadaException("Ya existe una factura con serie '" + serie + "' y folio '" + folio + "'");
    }
    factura.setIdFactura(null);
    if (factura.getFecha() == null) {
      factura.setFecha(Instant.now());
    }
    if (factura.getEstatus() == null || factura.getEstatus().isBlank()) {
      factura.setEstatus("Cargada");
    }
    if (factura.getSubtotal() == null) {
      factura.setSubtotal(BigDecimal.ZERO);
    }
    if (factura.getImpuesto() == null) {
      factura.setImpuesto(BigDecimal.ZERO);
    }
    if (factura.getTotal() == null) {
      factura.setTotal(BigDecimal.ZERO);
    }
    factura.setCreatedAt(Instant.now());
    factura.setUpdatedAt(Instant.now());
    return repository.save(factura);
  }

  @Override
  @Transactional(readOnly = true)
  public Factura obtenerPorId(Long idFactura) {
    if (idFactura == null) {
      throw new IllegalArgumentException("El id de factura es obligatorio");
    }
    return repository.findById(idFactura)
        .orElseThrow(() -> new FacturaNoEncontradaException("Factura no encontrada con id " + idFactura));
  }

  @Override
  @Transactional(readOnly = true)
  public Factura obtenerPorUuid(String uuid) {
    if (uuid == null || uuid.isBlank()) {
      throw new IllegalArgumentException("El UUID es obligatorio");
    }
    return repository.findByUuid(uuid.trim())
        .orElseThrow(() -> new FacturaNoEncontradaException("Factura no encontrada con UUID " + uuid));
  }

  @Override
  @Transactional(readOnly = true)
  public Factura obtenerPorNoTicket(String noTicket) {
    if (noTicket == null || noTicket.isBlank()) {
      throw new IllegalArgumentException("El numero de ticket es obligatorio");
    }
    String limpio = noTicket.trim();
    return repository.findByNoTicket(limpio)
        .orElseThrow(() -> new FacturaNoEncontradaException("Factura no encontrada con ticket " + limpio));
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResult<Factura> listar(Instant desde, Instant hasta, String noTicket,
                                     int page, int size, String sort) {
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 100);
    return repository.findConFiltros(desde, hasta, noTicket, safePage, safeSize, sort);
  }

  @Transactional(readOnly = true)
  public boolean existePorNoTicket(String noTicket) {
    if (noTicket == null || noTicket.isBlank()) {
      return false;
    }
    return repository.existsByNoTicket(noTicket.trim());
  }

  @Transactional(readOnly = true)
  public boolean existePorSerieYFolio(String serie, String folio) {
    if (folio == null || folio.isBlank()) {
      return false;
    }
    String serieLimpia = serie == null ? "" : serie.trim();
    return repository.existsBySerieAndFolio(serieLimpia, folio.trim());
  }

  @Override
  @Transactional
  public Factura actualizarPorId(Long idFactura, Factura cambios) {
    Factura actual = obtenerPorId(idFactura);
    validarObligatorios(cambios);
    String serieNueva = normalizarSerie(cambios.getSerie());
    String folioNuevo = cambios.getFolio().trim();
    if (!serieNueva.equals(actual.getSerie()) || !folioNuevo.equals(actual.getFolio())) {
      if (repository.existsBySerieAndFolio(serieNueva, folioNuevo)) {
        throw new FacturaDuplicadaException("Ya existe una factura con serie '" + serieNueva + "' y folio '" + folioNuevo + "'");
      }
    }
    actual.setFolio(folioNuevo);
    actual.setSerie(serieNueva);
    actual.setFecha(cambios.getFecha() == null ? actual.getFecha() : cambios.getFecha());
    actual.setCliente(cambios.getCliente());
    actual.setRfc(cambios.getRfc().trim().toUpperCase());
    actual.setRazonSocial(cambios.getRazonSocial());
    actual.setNoTicket(cambios.getNoTicket());
    actual.setSubtotal(cambios.getSubtotal() == null ? BigDecimal.ZERO : cambios.getSubtotal());
    actual.setImpuesto(cambios.getImpuesto() == null ? BigDecimal.ZERO : cambios.getImpuesto());
    actual.setTotal(cambios.getTotal() == null ? BigDecimal.ZERO : cambios.getTotal());
    actual.setEstatus(cambios.getEstatus() == null || cambios.getEstatus().isBlank() ? actual.getEstatus() : cambios.getEstatus());
    actual.setUuid(cambios.getUuid());
    actual.setUpdatedAt(Instant.now());
    return repository.save(actual);
  }

  @Override
  @Transactional
  public void eliminarPorId(Long idFactura) {
    Factura actual = obtenerPorId(idFactura);
    actual.setEstatus("CANCELADA");
    actual.setUpdatedAt(Instant.now());
    repository.save(actual);
  }

  private void validarObligatorios(Factura factura) {
    if (factura == null) {
      throw new IllegalArgumentException("Los datos de la factura son obligatorios");
    }
    if (factura.getFolio() == null || factura.getFolio().isBlank()) {
      throw new IllegalArgumentException("El folio es obligatorio");
    }
    if (factura.getRfc() == null || factura.getRfc().isBlank()) {
      throw new IllegalArgumentException("El RFC es obligatorio");
    }
    if (factura.getRazonSocial() == null || factura.getRazonSocial().isBlank()) {
      throw new IllegalArgumentException("La razon social es obligatoria");
    }
    if (factura.getNoTicket() == null || factura.getNoTicket().isBlank()) {
      throw new IllegalArgumentException("El numero de ticket es obligatorio");
    }
  }

  private String normalizarSerie(String serie) {
    if (serie == null || serie.isBlank()) {
      return "";
    }
    return serie.trim().toUpperCase();
  }
}
