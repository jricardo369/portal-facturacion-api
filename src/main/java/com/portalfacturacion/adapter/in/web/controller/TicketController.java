package com.portalfacturacion.adapter.in.web.controller;

import com.portalfacturacion.adapter.in.web.response.TicketItemResponse;
import com.portalfacturacion.adapter.in.web.response.TicketPagoResponse;
import com.portalfacturacion.adapter.in.web.response.TicketResponse;
import com.portalfacturacion.application.service.TicketService;
import com.portalfacturacion.domain.model.Ticket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets", description = "Consulta de tickets desde FuDo para el portal de bienvenida")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class TicketController {

  private final TicketService service;

  public TicketController(TicketService service) {
    this.service = service;
  }

  @GetMapping("/{numeroTicket}")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR','CONSULTA')")
  @Operation(summary = "Obtener datos de un ticket de FuDo por folio",
      description = "Endpoint publico para el portal de bienvenida. "
          + "El folio se mapea al id de venta (sale) de FuDo. "
          + "Los parametros transaccion, rfc y codigoPostal son opcionales y se conservan "
          + "para trazabilidad y validacion futura contra el ticket impreso.")
  public ResponseEntity<TicketResponse> obtener(
      @PathVariable @Pattern(regexp = "^[A-Za-z0-9\\-]{1,30}$", message = "Formato de ticket invalido") String numeroTicket,
      @RequestParam(required = false) String transaccion,
      @RequestParam(required = false) String rfc,
      @RequestParam(required = false) String codigoPostal) {
    Ticket ticket = service.obtenerTicket(numeroTicket);
    return ResponseEntity.ok(toResponse(ticket));
  }

  private TicketResponse toResponse(Ticket t) {
    return new TicketResponse(t.getNumeroTicket(), t.getTotal(), t.getSubtotal(), t.getImpuestos(),
        t.getEstado(), t.getTipoVenta(), t.getFechaEmision(), t.getFechaCierre(), t.getCliente(),
        t.getMesa(), t.getSucursal(), t.isFacturable(),
        t.getItems() == null ? java.util.List.of() : t.getItems().stream()
            .map(i -> new TicketItemResponse(i.getId(), i.getDescripcion(), i.getCantidad(),
                i.getPrecioUnitario(), i.getImporte())).toList(),
        t.getPagos() == null ? java.util.List.of() : t.getPagos().stream()
            .map(p -> new TicketPagoResponse(p.getId(), p.getMetodo(), p.getMonto())).toList());
  }
}
