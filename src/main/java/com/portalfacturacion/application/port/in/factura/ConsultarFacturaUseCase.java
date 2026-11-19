package com.portalfacturacion.application.port.in.factura;

import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.domain.model.Factura;
import java.time.Instant;

public interface ConsultarFacturaUseCase {
  Factura obtenerPorId(Long idFactura);
  Factura obtenerPorUuid(String uuid);
  Factura obtenerPorNoTicket(String noTicket);
  PagedResult<Factura> listar(Instant desde, Instant hasta, String noTicket, String cliente, String rfc,
      String estatus, String uuid, Instant createdAt, java.math.BigDecimal total, int page, int size, String sort);
}
