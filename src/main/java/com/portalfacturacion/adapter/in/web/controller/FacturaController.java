package com.portalfacturacion.adapter.in.web.controller;

import com.portalfacturacion.adapter.in.web.response.FacturaResponse;
import com.portalfacturacion.adapter.in.web.response.PaginaFacturaResponse;
import com.portalfacturacion.adapter.in.web.response.TicketFacturaResponse;
import com.portalfacturacion.adapter.in.web.response.TicketItemResponse;
import com.portalfacturacion.adapter.in.web.response.TicketPagoResponse;
import com.portalfacturacion.adapter.in.web.response.TicketResponse;
import com.portalfacturacion.application.port.out.fudo.FudoTicketPort;
import com.portalfacturacion.application.service.ConsultaFacturaService;
import com.portalfacturacion.domain.model.Factura;
import com.portalfacturacion.domain.model.Ticket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.function.Function;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/facturas")
@Tag(name = "Factura", description = "Consulta paginada de facturas")
@Validated
public class FacturaController {

  private final ConsultaFacturaService consultaFacturaService;
  private final FudoTicketPort fudoTicketPort;

  public FacturaController(ConsultaFacturaService consultaFacturaService, FudoTicketPort fudoTicketPort) {
    this.consultaFacturaService = consultaFacturaService;
    this.fudoTicketPort = fudoTicketPort;
  }

  @GetMapping
  @Operation(summary = "Consultar facturas con paginación y filtros desde/hasta/noTicket",
      description = "Filtros opcionales combinables: desde, hasta (rango de fecha) y noTicket.")
  public ResponseEntity<PaginaFacturaResponse> consultar(
      @RequestParam(required = false) Instant desde,
      @RequestParam(required = false) Instant hasta,
      @RequestParam(required = false) String noTicket,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String sort) {
    var pageResult = consultaFacturaService.getFacturas(desde, hasta, noTicket, page, size, sort);
    Function<Factura, FacturaResponse> mapper = f -> new FacturaResponse(
        f.getIdFactura(), f.getFolio(), f.getSerie(), f.getFecha(),
        f.getCliente(), f.getRfc(), f.getRazonSocial(), f.getNoTicket(),
        f.getSubtotal(), f.getImpuesto(), f.getIva(), f.getIsr(), f.getCedular(), f.getTotal(),
        f.getEstatus(), f.getUuid(), f.getFacturapiId(), f.getCancellationStatus(),
        f.getCreatedAt(), f.getUpdatedAt(),
        f.getTickets().stream().map(this::toTicketFacturaResponse).toList());
    return ResponseEntity.ok(PaginaFacturaResponse.from(pageResult, mapper));
  }

  private TicketFacturaResponse toTicketFacturaResponse(com.portalfacturacion.domain.model.TicketFactura t) {
    return new TicketFacturaResponse(t.getId(), t.getNumeroTicket(), t.getFechaEmision(), t.getHora(),
        t.getSubtotal(), t.getIva(), t.getIsr(), t.getCedular(), t.getTotal());
  }
}
