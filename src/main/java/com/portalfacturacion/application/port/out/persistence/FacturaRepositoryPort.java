package com.portalfacturacion.application.port.out.persistence;

import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.domain.model.Factura;
import com.portalfacturacion.domain.model.Ticket;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface FacturaRepositoryPort {
  Factura save(Factura factura);
  Optional<Factura> findById(Long idFactura);
  Optional<Factura> findByUuid(String uuid);
  Optional<Factura> findBySerieAndFolio(String serie, String folio);
  boolean existsBySerieAndFolio(String serie, String folio);
  Optional<Factura> findByNoTicket(String noTicket);
  boolean existsByNoTicket(String noTicket);
  PagedResult<Factura> findConFiltros(Instant desde, Instant hasta, String noTicket, int page, int size, String sort);
  void guardarTicketsFactura(Long idFactura, List<Ticket> tickets);
}
