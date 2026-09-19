package com.portalfacturacion.application.service;

import com.portalfacturacion.domain.exception.ClienteNoEncontradoException;
import com.portalfacturacion.domain.exception.FacturaDuplicadaException;
import com.portalfacturacion.domain.exception.FacturaNoEncontradaException;
import com.portalfacturacion.domain.model.Cliente;
import com.portalfacturacion.domain.model.DatosFactura;
import com.portalfacturacion.domain.model.Factura;
import com.portalfacturacion.domain.model.Ticket;
import com.portalfacturacion.application.port.out.notification.CorreoPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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

  @Transactional(readOnly = true)
  public DatosFactura buscarFacturaPorFiltro(String filtro) {
    if (filtro == null || filtro.isBlank()) {
      throw new IllegalArgumentException("El filtro es obligatorio");
    }
    String valor = filtro.trim();
    Factura factura;
    try {
      factura = facturaService.obtenerPorNoTicket(valor);
    } catch (FacturaNoEncontradaException ignored) {
      try {
        factura = facturaService.obtenerPorUuid(valor);
      } catch (FacturaNoEncontradaException ex) {
        throw new FacturaNoEncontradaException(
            "No se encontro la factura con los datos proporcionados");
      }
    }
    Cliente cliente = null;
    if (factura.getRfc() != null && !factura.getRfc().isBlank()) {
      try {
        cliente = clienteService.obtenerPorRfc(factura.getRfc().trim());
      } catch (ClienteNoEncontradoException ignored) {
        cliente = null;
      }
    }
    return new DatosFactura(toTicket(factura), cliente);
  }

  private Ticket toTicket(Factura factura) {
    if (factura == null) {
      return null;
    }
    return new Ticket(factura.getNoTicket(), factura.getTotal(), factura.getSubtotal(),
        factura.getImpuesto(), factura.getEstatus(), null,
        factura.getFecha(), factura.getFecha(), factura.getCliente(), null, null,
        false, List.of(), List.of());
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

  @Transactional
  public DatosFactura refacturar(DatosFactura datos) {
    if (datos == null || datos.getCliente() == null
        || datos.getCliente().getRfc() == null
        || datos.getCliente().getRfc().isBlank()) {
      throw new IllegalArgumentException("Los datos del cliente y RFC son obligatorios para refacturar");
    }
    Cliente guardado = guardarClienteSiCambio(datos.getCliente());
    Factura factura = insertarFacturaRefacturada(datos.getTicket(), guardado);
    CorreoPreparado preparado = plantillas.construirCorreo("correo_factura.html",
      "titulo=" + "Refacturación Oso Despierto",
      "folio=" + factura.getFolio(),
      "total=" + (factura.getTotal() != null ? factura.getTotal().toPlainString() : "0.00"),
      "cliente=" + (factura.getCliente() != null ? factura.getCliente() : "Sin nombre"));
    enviarCorreoTrasCommit(guardado.getCorreoElectronico(), preparado.asunto(), preparado.contenido());
    return new DatosFactura(datos.getTicket(), guardado);
  }

  private Cliente guardarClienteSiCambio(Cliente datos) {
    String rfc = datos.getRfc().trim();
    try {
      Cliente actual = clienteService.obtenerPorRfc(rfc);
      if (hayCambiosEnCliente(actual, datos)) {
        return clienteService.actualizarPorRfc(rfc, datos);
      }
      return actual;
    } catch (ClienteNoEncontradoException ignored) {
      return clienteService.crear(datos);
    }
  }

  private boolean hayCambiosEnCliente(Cliente actual, Cliente datos) {
    return !Objects.equals(actual.getRazonSocial(), datos.getRazonSocial())
        || !Objects.equals(actual.getCalle(), datos.getCalle())
        || !Objects.equals(actual.getNumExterior(), datos.getNumExterior())
        || !Objects.equals(actual.getNumInterior(), datos.getNumInterior())
        || !Objects.equals(actual.getReferencia(), datos.getReferencia())
        || !Objects.equals(actual.getEstado(), datos.getEstado())
        || !Objects.equals(actual.getMunicipio(), datos.getMunicipio())
        || !Objects.equals(actual.getColonia(), datos.getColonia())
        || !Objects.equals(actual.getCodigoPostal(), datos.getCodigoPostal())
        || !Objects.equals(actual.getCorreoElectronico(), datos.getCorreoElectronico())
        || !Objects.equals(actual.getRegimenFiscal(), datos.getRegimenFiscal())
        || !Objects.equals(actual.getUsoFactura(), datos.getUsoFactura());
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
    return crearFactura(ticket, cliente, noTicket, noTicket, null);
  }

  private Factura insertarFacturaRefacturada(Ticket ticket, Cliente cliente) {
    if (ticket == null || ticket.getNumeroTicket() == null || ticket.getNumeroTicket().isBlank()) {
      throw new IllegalArgumentException("El numero de ticket es obligatorio para refacturar");
    }
    String noTicket = ticket.getNumeroTicket().trim();
    cancelarFacturaPrevia(noTicket);
    String nuevoFolio = generarFolioRefactura(noTicket);
    return crearFactura(ticket, cliente, nuevoFolio, nuevoFolio, UUID.randomUUID().toString());
  }

  private void cancelarFacturaPrevia(String noTicket) {
    try {
      Factura previa = facturaService.obtenerPorNoTicket(noTicket);
      if (previa != null && !"CANCELADA".equalsIgnoreCase(previa.getEstatus())) {
        facturaService.eliminarPorId(previa.getIdFactura());
      }
    } catch (FacturaNoEncontradaException ignored) {
    }
  }

  private String generarFolioRefactura(String noTicket) {
    String base = noTicket;
    String sufijo = "-R";
    String folio = truncarFolio(base, sufijo);
    int intento = 2;
    while (facturaService.existePorSerieYFolio("", folio)) {
      folio = truncarFolio(base, "-" + intento++);
    }
    return folio;
  }

  private String truncarFolio(String base, String sufijo) {
    String candidato = base + sufijo;
    if (candidato.length() <= 30) {
      return candidato;
    }
    return base.substring(0, Math.max(1, 30 - sufijo.length())) + sufijo;
  }

  private Factura crearFactura(Ticket ticket, Cliente cliente, String folio, String noTicket, String uuid) {
    Factura factura = new Factura();
    factura.setFolio(folio);
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
    factura.setUuid(uuid);
    return facturaService.crear(factura);
  }
}
