package com.portalfacturacion.application.service;

import com.portalfacturacion.application.port.in.factura.ConsultarFacturaUseCase;
import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.domain.model.Factura;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class ConsultaFacturaService {

  private final ConsultarFacturaUseCase consultarFacturaUseCase;

  public ConsultaFacturaService(ConsultarFacturaUseCase consultarFacturaUseCase) {
    this.consultarFacturaUseCase = consultarFacturaUseCase;
  }

  public PagedResult<Factura> getFacturas(Instant desde, Instant hasta, String noTicket,
      int page, int size, String sort) {
    return consultarFacturaUseCase.listar(desde, hasta, noTicket, page, size, sort);
  }
}
