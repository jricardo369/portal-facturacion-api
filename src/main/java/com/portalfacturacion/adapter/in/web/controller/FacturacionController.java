package com.portalfacturacion.adapter.in.web.controller;

import com.portalfacturacion.adapter.in.web.response.ClienteResponse;
import com.portalfacturacion.adapter.in.web.response.DatosFacturaResponse;
import com.portalfacturacion.adapter.in.web.response.FacturaResponse;
import com.portalfacturacion.adapter.in.web.response.TicketItemResponse;
import com.portalfacturacion.adapter.in.web.response.TicketPagoResponse;
import com.portalfacturacion.application.service.FacturacionService;
import com.portalfacturacion.domain.model.Cliente;
import com.portalfacturacion.domain.model.DatosFactura;
import com.portalfacturacion.domain.model.Factura;
import com.portalfacturacion.domain.model.Ticket;
import com.portalfacturacion.domain.model.TicketItem;
import com.portalfacturacion.domain.model.TicketPago;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/facturacion")
@Tag(name = "Facturacion", description = "Combina ticket de FuDo y cliente local para previsualizar datos de factura")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class FacturacionController {

  private final FacturacionService service;

  public FacturacionController(FacturacionService service) {
    this.service = service;
  }

  @GetMapping("/datos-factura")
  @Operation(summary = "Obtener datos de factura por query params rfc y numeroTicket")
  public ResponseEntity<DatosFacturaResponse> obtenerPorParams(
      @RequestParam(required = false) String rfc,
      @RequestParam String numeroTicket) {
    return ResponseEntity.ok(toResponse(service.obtenerDatosFactura(rfc, numeroTicket)));
  }

  @PostMapping("/facturar")
  @Operation(summary = "Facturar: hace upsert de cliente y retorna datos listos para timbrar")
  public ResponseEntity<DatosFacturaResponse> facturar(
      @RequestBody DatosFacturaResponse request) {
    return ResponseEntity.ok(toResponse(service.facturar(toDomain(request))));
  }

  @PostMapping("/refacturar")
  @Operation(summary = "Refacturar: cancela la factura previa del ticket, inserta una nueva "
      + "factura con UUID nuevo y actualiza el cliente si sus datos cambiaron")
  public ResponseEntity<DatosFacturaResponse> refacturar(
      @RequestBody DatosFacturaResponse request) {
    return ResponseEntity.ok(toResponse(service.refacturar(toDomain(request))));
  }

  @GetMapping("/factura")
  @Operation(summary = "Obtener factura por numero de ticket")
  public ResponseEntity<FacturaResponse> obtenerFacturaPorTicket(
      @RequestParam String numeroTicket) {
    return ResponseEntity.ok(toFacturaResponse(service.obtenerFacturaPorTicket(numeroTicket)));
  }

  @GetMapping("/factura/buscar")
  @Operation(summary = "Buscar factura por no ticket o uuid, incluye datos del cliente")
  public ResponseEntity<DatosFacturaResponse> buscarFacturaPorFiltro(
      @RequestParam String filtro) {
    return ResponseEntity.ok(toResponse(service.buscarFacturaPorFiltro(filtro)));
  }

  private DatosFactura toDomain(DatosFacturaResponse r) {
    if (r == null) {
      throw new IllegalArgumentException("El cuerpo con los datos de factura es obligatorio");
    }
    List<TicketItem> items = r.items() == null ? List.of() : r.items().stream()
        .map(i -> new TicketItem(i.id(), i.descripcion(), i.cantidad(),
            i.precioUnitario(), i.importe())).toList();
    List<TicketPago> pagos = r.pagos() == null ? List.of() : r.pagos().stream()
        .map(p -> new TicketPago(p.id(), p.metodo(), p.monto())).toList();
    Ticket ticket = new Ticket(r.numeroTicket(), r.total(), r.subtotal(), r.impuestos(),
        null, r.tipoVenta(), r.fechaEmision(), r.fechaCierre(),
        r.clienteTicket(), null, null, r.facturable(), items, pagos);
    return new DatosFactura(ticket, toClienteDomain(r.cliente()));
  }

  private Cliente toClienteDomain(ClienteResponse c) {
    if (c == null) {
      return null;
    }
    return new Cliente(c.idCliente(), c.rfc(), c.razonSocial(), c.calle(),
        c.numExterior(), c.numInterior(), c.referencia(), c.estado(),
        c.municipio(), c.colonia(), c.codigoPostal(), c.correoElectronico(),
        c.regimenFiscal(), c.usoFactura(), c.estatus(),
        c.createdAt(), c.updatedAt());
  }

  private DatosFacturaResponse toResponse(DatosFactura datos) {
    Ticket t = datos.getTicket();
    List<TicketItemResponse> items = t.getItems() == null ? List.of() : t.getItems().stream()
        .map(i -> new TicketItemResponse(i.getId(), i.getDescripcion(), i.getCantidad(),
            i.getPrecioUnitario(), i.getImporte())).toList();
    List<TicketPagoResponse> pagos = t.getPagos() == null ? List.of() : t.getPagos().stream()
        .map(p -> new TicketPagoResponse(p.getId(), p.getMetodo(), p.getMonto())).toList();
    return new DatosFacturaResponse(t.getNumeroTicket(), t.getTotal(), t.getSubtotal(),
        t.getImpuestos(), t.getTipoVenta(), t.getFechaEmision(), t.getFechaCierre(),
        t.getCliente(), t.isFacturable(), items, pagos, resolverTipoPago(pagos),
        toClienteResponse(datos.getCliente()));
  }

  private String resolverTipoPago(List<TicketPagoResponse> pagos) {
    if (pagos == null || pagos.isEmpty()) {
      return null;
    }
    if (pagos.size() == 1) {
      return pagos.get(0).metodo();
    }
    return pagos.stream().map(TicketPagoResponse::metodo).distinct()
        .collect(java.util.stream.Collectors.joining(" + "));
  }

  private ClienteResponse toClienteResponse(Cliente c) {
    if (c == null) {
      return null;
    }
    return new ClienteResponse(c.getIdCliente(), c.getRfc(), c.getRazonSocial(), c.getCalle(),
        c.getNumExterior(), c.getNumInterior(), c.getReferencia(), c.getEstado(),
        c.getMunicipio(), c.getColonia(), c.getCodigoPostal(), c.getCorreoElectronico(),
        c.getRegimenFiscal(), c.getUsoFactura(), c.getEstatus(),
        c.getCreatedAt(), c.getUpdatedAt());
  }

  private FacturaResponse toFacturaResponse(Factura f) {
    return new FacturaResponse(f.getIdFactura(), f.getFolio(), f.getSerie(), f.getFecha(),
        f.getCliente(), f.getRfc(), f.getRazonSocial(), f.getNoTicket(),
        f.getSubtotal(), f.getImpuesto(), f.getTotal(),
        f.getEstatus(), f.getUuid(), f.getCreatedAt(), f.getUpdatedAt());
  }
}
