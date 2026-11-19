package com.portalfacturacion.adapter.in.web.controller;

import com.portalfacturacion.adapter.in.web.response.ClienteResponse;
import com.portalfacturacion.adapter.in.web.response.DatosFacturaResponse;
import com.portalfacturacion.adapter.in.web.response.FacturaResponse;
import com.portalfacturacion.adapter.in.web.response.TicketItemResponse;
import com.portalfacturacion.adapter.in.web.response.TicketFacturaResponse;
import com.portalfacturacion.adapter.in.web.response.TicketPagoResponse;
import com.portalfacturacion.application.service.FacturacionService;
import com.portalfacturacion.domain.model.Cliente;
import com.portalfacturacion.domain.model.DatosFactura;
import com.portalfacturacion.domain.model.Factura;
import com.portalfacturacion.domain.model.Ticket;
import com.portalfacturacion.domain.model.TicketItem;
import com.portalfacturacion.domain.model.TicketPago;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
  @Operation(summary = "Obtener datos de factura por query params rfc, numeroTicket, fecha y total")
  public ResponseEntity<DatosFacturaResponse> obtenerPorParams(
      @RequestParam(required = false) String rfc,
      @RequestParam String numeroTicket,
      @Parameter(description = "Fecha del ticket (yyyy-MM-dd). Se valida contra la fecha del ticket en FuDo")
      @RequestParam(required = false) String fecha,
      @Parameter(description = "Total del ticket. Se valida contra el total del ticket en FuDo")
      @RequestParam(required = false) BigDecimal total) {
    return ResponseEntity.ok(toResponse(service.obtenerDatosFactura(rfc, numeroTicket, fecha, total)));
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

  @PostMapping("/factura/cancelar")
  @Operation(summary = "Cancelar factura timbrada ante el SAT via Facturapi",
      description = "Motivos SAT: 02 (errores sin relacion), 03 (no se llevo a cabo la operacion). "
          + "El motivo 01 se gestiona desde refacturar porque requiere la factura sustituta.")
  public ResponseEntity<FacturaResponse> cancelar(
      @RequestParam String uuid,
      @Parameter(description = "Motivo de cancelacion SAT. Default: configuracion app.facturapi")
      @RequestParam(required = false) String motivo) {
    return ResponseEntity.ok(toFacturaResponse(service.cancelarFactura(uuid, motivo)));
  }

  @GetMapping("/factura/xml")
  @Operation(summary = "Descargar XML del CFDI timbrado por uuid")
  public ResponseEntity<byte[]> descargarXml(@RequestParam String uuid) {
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_XML)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cfdi-" + uuid + ".xml\"")
        .body(service.descargarXml(uuid));
  }

  @GetMapping("/factura/pdf")
  @Operation(summary = "Descargar PDF del CFDI timbrado por uuid")
  public ResponseEntity<byte[]> descargarPdf(@RequestParam String uuid) {
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cfdi-" + uuid + ".pdf\"")
        .body(service.descargarPdf(uuid));
  }

  @GetMapping("/factura/zip")
  @Operation(summary = "Descargar ZIP con XML y PDF del CFDI timbrado por uuid")
  public ResponseEntity<byte[]> descargarZip(@RequestParam String uuid) {
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cfdi-" + uuid + ".zip\"")
        .body(service.descargarZip(uuid));
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
    ticket.setIva(r.iva());
    ticket.setIsr(r.isr());
    ticket.setCedular(r.cedular());
    List<Ticket> tickets = new ArrayList<>();
    if (r.tickets() != null && !r.tickets().isEmpty()) {
      for (DatosFacturaResponse t : r.tickets()) {
        List<TicketItem> tkItems = t.items() == null ? List.of() : t.items().stream()
            .map(i -> new TicketItem(i.id(), i.descripcion(), i.cantidad(), i.precioUnitario(), i.importe()))
            .toList();
        List<TicketPago> tkPagos = t.pagos() == null ? List.of() : t.pagos().stream()
            .map(p -> new TicketPago(p.id(), p.metodo(), p.monto())).toList();
        Ticket tk = new Ticket(t.numeroTicket(), t.total(), t.subtotal(), t.impuestos(),
            null, t.tipoVenta(), t.fechaEmision(), t.fechaCierre(),
            t.clienteTicket(), null, null, t.facturable(), tkItems, tkPagos);
        tk.setIva(t.iva());
        tk.setIsr(t.isr());
        tk.setCedular(t.cedular());
        tickets.add(tk);
      }
    } else {
      tickets.add(ticket);
    }
    return new DatosFactura(tickets, toClienteDomain(r.cliente()));
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
    List<DatosFacturaResponse> ticketsResponse = new ArrayList<>();
    List<Ticket> tickets = datos.getTickets() != null && !datos.getTickets().isEmpty()
        ? datos.getTickets()
        : List.of(t);
    for (Ticket tk : tickets) {
      List<TicketItemResponse> tkItems = tk.getItems() == null ? List.of() : tk.getItems().stream()
          .map(i -> new TicketItemResponse(i.getId(), i.getDescripcion(), i.getCantidad(),
              i.getPrecioUnitario(), i.getImporte())).toList();
      List<TicketPagoResponse> tkPagos = tk.getPagos() == null ? List.of() : tk.getPagos().stream()
          .map(p -> new TicketPagoResponse(p.getId(), p.getMetodo(), p.getMonto())).toList();
      ticketsResponse.add(new DatosFacturaResponse(tk.getNumeroTicket(), tk.getTotal(), tk.getSubtotal(),
          tk.getImpuestos(), tk.getIva(), tk.getIsr(), tk.getCedular(), tk.getTipoVenta(),
          tk.getFechaEmision(), tk.getFechaCierre(), tk.getCliente(), tk.isFacturable(), tkItems, tkPagos,
          resolverTipoPago(tkPagos), toClienteResponse(datos.getCliente()), List.of()));
    }
    BigDecimal total = BigDecimal.ZERO;
    BigDecimal subtotal = BigDecimal.ZERO;
    BigDecimal impuesto = BigDecimal.ZERO;
    BigDecimal iva = BigDecimal.ZERO;
    BigDecimal isr = BigDecimal.ZERO;
    BigDecimal cedular = BigDecimal.ZERO;
    for (Ticket tk : tickets) {
      total = total.add(tk.getTotal() != null ? tk.getTotal() : BigDecimal.ZERO);
      subtotal = subtotal.add(tk.getSubtotal() != null ? tk.getSubtotal() : BigDecimal.ZERO);
      impuesto = impuesto.add(tk.getImpuestos() != null ? tk.getImpuestos() : BigDecimal.ZERO);
      iva = iva.add(tk.getIva() != null ? tk.getIva() : BigDecimal.ZERO);
      isr = isr.add(tk.getIsr() != null ? tk.getIsr() : BigDecimal.ZERO);
      cedular = cedular.add(tk.getCedular() != null ? tk.getCedular() : BigDecimal.ZERO);
    }
    return new DatosFacturaResponse(tickets.get(0).getNumeroTicket(), total, subtotal,
        impuesto, iva, isr, cedular, tickets.get(0).getTipoVenta(),
        tickets.get(0).getFechaEmision(), tickets.get(0).getFechaCierre(),
        tickets.get(0).getCliente(), tickets.get(0).isFacturable(), items, pagos, resolverTipoPago(pagos),
        toClienteResponse(datos.getCliente()), ticketsResponse);
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

  private TicketFacturaResponse toTicketFacturaResponse(com.portalfacturacion.domain.model.TicketFactura t) {
    return new TicketFacturaResponse(t.getId(), t.getNumeroTicket(), t.getFechaEmision(), t.getHora(),
        t.getSubtotal(), t.getIva(), t.getIsr(), t.getCedular(), t.getTotal());
  }

  private FacturaResponse toFacturaResponse(Factura f) {
    return new FacturaResponse(f.getIdFactura(), f.getFolio(), f.getSerie(), f.getFecha(),
        f.getCliente(), f.getRfc(), f.getRazonSocial(),
        f.getTickets().stream().map(t -> t.getNumeroTicket()).distinct().collect(java.util.stream.Collectors.joining(", ")),
        f.getSubtotal(), f.getImpuesto(), f.getIva(), f.getIsr(), f.getCedular(), f.getTotal(),
        f.getEstatus(), f.getUuid(), f.getFacturapiId(), f.getCancellationStatus(),
         f.getCreatedAt(), f.getUpdatedAt(), f.getTickets().stream().map(this::toTicketFacturaResponse).toList());
  }
}
