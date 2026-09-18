package com.portalfacturacion.application.service;

import com.portalfacturacion.domain.exception.ClienteNoEncontradoException;
import com.portalfacturacion.domain.exception.FacturaDuplicadaException;
import com.portalfacturacion.domain.model.Cliente;
import com.portalfacturacion.domain.model.DatosFactura;
import com.portalfacturacion.domain.model.Factura;
import com.portalfacturacion.domain.model.Ticket;
import com.portalfacturacion.application.port.out.notification.CorreoPort;
import com.portalfacturacion.application.service.PlantillaCorreoService;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class FacturacionService {

  private final TicketService ticketService;
  private final ClienteService clienteService;
  private final FacturaService facturaService;
  private final PlantillaCorreoService plantillas;
  private final CorreoPort correo;

  public FacturacionService(TicketService ticketService, ClienteService clienteService,
                              FacturaService facturaService, PlantillaCorreoService plantillas,
                              CorreoPort correo) {
    this.ticketService = ticketService;
    this.clienteService = clienteService;
    this.facturaService = facturaService;
    this.plantillas = plantillas;
    this.correo = correo;
  }

  public DatosFactura obtenerDatosFactura(String rfc, String numeroTicket) {
    if (numeroTicket == null || numeroTicket.isBlank()) {
      throw new IllegalArgumentException("El numero de ticket es obligatorio");
    }
    String noTicket = numeroTicket.trim();
    if (facturaService.existePorNoTicket(noTicket)) {
      throw new FacturaDuplicadaException("El ticket " + noTicket + " ya ha sido facturado");
    }
    Ticket ticket = ticketService.obtenerTicket(noTicket);
    Cliente cliente = null;
    if (rfc != null && !rfc.isBlank()) {
      try {
        cliente = clienteService.obtenerPorRfc(rfc.trim());
      } catch (ClienteNoEncontradoException ignored) {
        cliente = null;
      }
    }
    aplicarImpuestosSegunRfc(ticket, rfc);
    return new DatosFactura(ticket, cliente);
  }

  private void aplicarImpuestosSegunRfc(Ticket ticket, String rfc) {
    if (ticket == null || rfc == null || rfc.isBlank()) {
      return;
    }
    String rfcLimpio = rfc.trim();
    if (rfcLimpio.length() == 12) {
      BigDecimal subtotal = ticket.getSubtotal() != null ? ticket.getSubtotal() : BigDecimal.ZERO;
      ticket.setImpuestos(subtotal.multiply(new BigDecimal("0.10")));
    } else if (ticket.getImpuestos() != null) {
      ticket.setImpuestos(BigDecimal.ZERO);
    }
  }

  @Transactional(readOnly = true)
  public Factura obtenerFacturaPorTicket(String numeroTicket) {
    if (numeroTicket == null || numeroTicket.isBlank()) {
      throw new IllegalArgumentException("El numero de ticket es obligatorio");
    }
    return facturaService.obtenerPorNoTicket(numeroTicket.trim());
  }

  @Transactional
  public DatosFactura facturar(DatosFactura datos) {
    if (datos == null || datos.getCliente() == null
        || datos.getCliente().getRfc() == null
        || datos.getCliente().getRfc().isBlank()) {
      throw new IllegalArgumentException("Los datos del cliente y RFC son obligatorios para facturar");
    }
    Cliente cliente = datos.getCliente();
    Cliente guardado;
    if (clienteService.existePorRfc(cliente.getRfc().trim())) {
      guardado = clienteService.actualizarPorRfc(cliente.getRfc().trim(), cliente);
    } else {
      guardado = clienteService.crear(cliente);
    }
    Factura factura = insertarFactura(datos.getTicket(), guardado);
    CorreoPreparado preparado = plantillas.construirCorreo("correo_factura.html",
      "titulo=" + "Facturación Oso Despierto",
      "folio=" + factura.getFolio(),
      "total=" + (factura.getTotal() != null ? factura.getTotal().toPlainString() : "0.00"),
      "cliente=" + (factura.getCliente() != null ? factura.getCliente() : "Sin nombre"));
    enviarCorreoTrasCommit(guardado.getCorreoElectronico(), preparado.asunto(), preparado.contenido());
    return new DatosFactura(datos.getTicket(), guardado);
  }

  private void enviarCorreoTrasCommit(String destinatario, String asunto, String contenido) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      correo.enviarTextoPlano(destinatario, asunto, contenido);
      return;
    }
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        correo.enviarTextoPlano(destinatario, asunto, contenido);
      }
    });
  }

  private Factura insertarFactura(Ticket ticket, Cliente cliente) {
    if (ticket == null || ticket.getNumeroTicket() == null || ticket.getNumeroTicket().isBlank()) {
      throw new IllegalArgumentException("El numero de ticket es obligatorio para facturar");
    }
    String noTicket = ticket.getNumeroTicket().trim();
    if (facturaService.existePorNoTicket(noTicket)) {
      throw new FacturaDuplicadaException("La factura ya ha sido generada para el ticket " + noTicket);
    }
    Factura factura = new Factura();
    factura.setFolio(noTicket);
    factura.setSerie("");
    if (ticket.getFechaCierre() != null) {
      factura.setFecha(ticket.getFechaCierre());
    } else if (ticket.getFechaEmision() != null) {
      factura.setFecha(ticket.getFechaEmision());
    } else {
      factura.setFecha(Instant.now());
    }
    factura.setCliente(cliente.getRazonSocial());
    factura.setRfc(cliente.getRfc());
    factura.setRazonSocial(cliente.getRazonSocial());
    factura.setNoTicket(noTicket);
    factura.setSubtotal(ticket.getSubtotal());
    factura.setImpuesto(ticket.getImpuestos());
    factura.setTotal(ticket.getTotal());
    factura.setEstatus("Cargada");
    factura.setUuid(null);
    factura = facturaService.crear(factura);
    return factura;
  }
}
