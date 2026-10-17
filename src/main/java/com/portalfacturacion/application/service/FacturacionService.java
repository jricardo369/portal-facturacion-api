package com.portalfacturacion.application.service;

import com.portalfacturacion.application.port.out.facturapi.FacturapiTimbradoPort;
import com.portalfacturacion.configuration.FacturapiProperties;
import com.portalfacturacion.domain.exception.ClienteNoEncontradoException;
import com.portalfacturacion.domain.exception.FacturaDuplicadaException;
import com.portalfacturacion.domain.exception.FacturaNoEncontradaException;
import com.portalfacturacion.domain.exception.TicketNoEncontradoException;
import com.portalfacturacion.domain.exception.TimbradoFallidoException;
import com.portalfacturacion.domain.model.Cliente;
import com.portalfacturacion.domain.model.DatosFactura;
import com.portalfacturacion.domain.model.Factura;
import com.portalfacturacion.domain.model.FacturaTimbrada;
import com.portalfacturacion.domain.model.ItemSolicitudTimbrado;
import com.portalfacturacion.domain.model.ResultadoCancelacion;
import com.portalfacturacion.domain.model.SolicitudTimbrado;
import com.portalfacturacion.domain.model.Ticket;
import com.portalfacturacion.domain.model.TicketPago;
import com.portalfacturacion.application.port.out.notification.CorreoPort;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class FacturacionService {

  private static final Logger log = LoggerFactory.getLogger(FacturacionService.class);

  private static final Pattern RFC_PATTERN = Pattern.compile("^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$");

  private final TicketService ticketService;
  private final ClienteService clienteService;
  private final FacturaService facturaService;
  private final PlantillaCorreoService plantillas;
  private final CorreoPort correo;
  private final FacturapiTimbradoPort facturapi;
  private final FacturapiProperties propiedades;

  public FacturacionService(TicketService ticketService, ClienteService clienteService,
                              FacturaService facturaService, PlantillaCorreoService plantillas,
                              CorreoPort correo, FacturapiTimbradoPort facturapi,
                              FacturapiProperties propiedades) {
    this.ticketService = ticketService;
    this.clienteService = clienteService;
    this.facturaService = facturaService;
    this.plantillas = plantillas;
    this.correo = correo;
    this.facturapi = facturapi;
    this.propiedades = propiedades;
  }

  public DatosFactura obtenerDatosFactura(String rfc, String numeroTicket) {
    return obtenerDatosFactura(rfc, numeroTicket, null, null);
  }

  public DatosFactura obtenerDatosFactura(String rfc, String numeroTicket,
                                          String fecha, BigDecimal total) {
    if (numeroTicket == null || numeroTicket.isBlank()) {
      throw new IllegalArgumentException("El numero de ticket es obligatorio");
    }
    String noTicket = numeroTicket.trim();
    if (facturaService.existePorNoTicket(noTicket)) {
      throw new FacturaDuplicadaException("El ticket " + noTicket + " ya ha sido facturado");
    }
    Ticket ticket = ticketService.obtenerTicket(noTicket);
    validarFechaContraTicket(ticket, fecha);
    validarTotalContraTicket(ticket, total);
    Cliente cliente = null;
    if (rfc != null && !rfc.isBlank()) {
      try {
        cliente = clienteService.obtenerPorRfc(rfc.trim());
      } catch (ClienteNoEncontradoException ignored) {
        cliente = null;
      }
    }
    return new DatosFactura(ticket, cliente);
  }

  private void validarFechaContraTicket(Ticket ticket, String fecha) {
    if (fecha == null || fecha.isBlank()) {
      return;
    }
    LocalDate esperada = parsearFecha(fecha.trim());
    Instant referencia = ticket.getFechaCierre() != null
        ? ticket.getFechaCierre() : ticket.getFechaEmision();
    if (referencia == null) {
      return;
    }
    ZoneId zona = ZoneId.of("America/Mexico_City");
    LocalDate fechaTicket = referencia.atZone(zona).toLocalDate();
    if (!fechaTicket.equals(esperada)
        && !referencia.atZone(ZoneId.of("UTC")).toLocalDate().equals(esperada)) {
      throw new TicketNoEncontradoException(
          "No se encontró el ticket con la fecha capturada");
    }
  }

  private LocalDate parsearFecha(String fecha) {
    try {
      return LocalDate.parse(fecha);
    } catch (DateTimeParseException ex) {
      try {
        return Instant.parse(fecha).atZone(ZoneId.of("America/Mexico_City")).toLocalDate();
      } catch (DateTimeParseException ex2) {
        throw new IllegalArgumentException("La fecha debe tener formato yyyy-MM-dd");
      }
    }
  }

  private void validarTotalContraTicket(Ticket ticket, BigDecimal total) {
    if (total == null) {
      return;
    }
    BigDecimal totalTicket = ticket.getTotal();
    if (totalTicket == null) {
      throw new TicketNoEncontradoException(
          "No se encontró el ticket con el total capturado");
    }
    BigDecimal diferencia = totalTicket.subtract(total).abs();
    if (diferencia.compareTo(new BigDecimal("0.01")) > 0) {
      throw new TicketNoEncontradoException(
          "No se encontró el ticket con el total capturado");
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
    if (datos == null || datos.getCliente() == null) {
      throw new IllegalArgumentException("Los datos del cliente son obligatorios para facturar");
    }
    Cliente cliente = datos.getCliente();
    cliente.setRfc(validarRfc(cliente.getRfc(), "facturar"));
    if (cliente.getRegimenFiscal() == null
        || cliente.getRegimenFiscal().isBlank()) {
      throw new IllegalArgumentException("El régimen fiscal es obligatorio para facturar");
    }
    cliente.setRegimenFiscal(cliente.getRegimenFiscal().trim());
    Cliente guardado;
    if (clienteService.existePorRfc(cliente.getRfc())) {
      guardado = clienteService.actualizarPorRfc(cliente.getRfc(), cliente);
    } else {
      guardado = clienteService.crear(cliente);
    }
    Factura factura = timbrarYRegistrar(datos.getTicket(), guardado);
    aplicarMontosTimbrados(datos.getTicket(), factura);
    CorreoPreparado preparado = plantillas.construirCorreo("correo_factura.html",
      "titulo=" + "Facturación Oso Despierto",
      "folio=" + factura.getFolio(),
      "total=" + (factura.getTotal() != null ? factura.getTotal().toPlainString() : "0.00"),
      "cliente=" + (factura.getCliente() != null ? factura.getCliente() : "Sin nombre"));
    enviarCorreoTrasCommit(guardado.getCorreoElectronico(), preparado.asunto(),
        preparado.contenido(), factura.getFacturapiId());
    return new DatosFactura(datos.getTicket(), guardado);
  }

  @Transactional
  public DatosFactura refacturar(DatosFactura datos) {
    if (datos == null || datos.getCliente() == null) {
      throw new IllegalArgumentException("Los datos del cliente son obligatorios para refacturar");
    }
    datos.getCliente().setRfc(validarRfc(datos.getCliente().getRfc(), "refacturar"));
    Cliente guardado = guardarClienteSiCambio(datos.getCliente());
    Factura factura = timbrarYRefacturar(datos.getTicket(), guardado);
    aplicarMontosTimbrados(datos.getTicket(), factura);
    CorreoPreparado preparado = plantillas.construirCorreo("correo_factura.html",
      "titulo=" + "Refacturación Oso Despierto",
      "folio=" + factura.getFolio(),
      "total=" + (factura.getTotal() != null ? factura.getTotal().toPlainString() : "0.00"),
      "cliente=" + (factura.getCliente() != null ? factura.getCliente() : "Sin nombre"));
    enviarCorreoTrasCommit(guardado.getCorreoElectronico(), preparado.asunto(),
        preparado.contenido(), factura.getFacturapiId());
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

  private void enviarCorreoTrasCommit(String destinatario, String asunto, String contenido,
                                      String facturapiId) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      enviarCorreoConAdjuntos(destinatario, asunto, contenido, facturapiId);
      return;
    }
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        enviarCorreoConAdjuntos(destinatario, asunto, contenido, facturapiId);
      }
    });
  }

  private void enviarCorreoConAdjuntos(String destinatario, String asunto, String contenido,
                                       String facturapiId) {
    try {
      if (facturapiId == null || facturapiId.isBlank()) {
        correo.enviarTextoPlano(destinatario, asunto, contenido);
        return;
      }
      byte[] zip = facturapi.obtenerZip(facturapiId);
      File adjunto = File.createTempFile("cfdi-", ".zip");
      adjunto.deleteOnExit();
      Files.write(adjunto.toPath(), zip);
      correo.enviarTextoPlanoConAdjuntos(destinatario, asunto, contenido, List.of(adjunto));
    } catch (Exception ex) {
      log.warn("No fue posible adjuntar el CFDI al correo a={}: {}", destinatario, ex.getMessage());
      correo.enviarTextoPlano(destinatario, asunto, contenido);
    }
  }

  @Transactional
  public Factura cancelarFactura(String uuid, String motivo) {
    Factura factura = facturaService.obtenerPorUuid(uuid);
    if (factura.getFacturapiId() == null || factura.getFacturapiId().isBlank()) {
      throw new TimbradoFallidoException("La factura no tiene comprobante timbrado en Facturapi");
    }
    if ("CANCELADA".equalsIgnoreCase(factura.getEstatus())) {
      throw new IllegalArgumentException("La factura ya se encuentra cancelada");
    }
    String motivoFinal = (motivo == null || motivo.isBlank())
        ? propiedades.getMotivoCancelacion() : motivo.trim();
    if ("01".equals(motivoFinal)) {
      throw new IllegalArgumentException(
          "El motivo 01 requiere la factura sustituta; utilice la opcion de refacturar");
    }
    ResultadoCancelacion resultado = facturapi.cancelar(factura.getFacturapiId(), motivoFinal, null);
    factura.setCancellationStatus(resultado.getCancellationStatus());
    if (resultado.aceptada()) {
      factura.setEstatus("CANCELADA");
    }
    return facturaService.actualizarPorId(factura.getIdFactura(), factura);
  }

  public byte[] descargarXml(String uuid) {
    return facturapi.obtenerXml(obtenerFacturapiId(uuid));
  }

  public byte[] descargarPdf(String uuid) {
    return facturapi.obtenerPdf(obtenerFacturapiId(uuid));
  }

  public byte[] descargarZip(String uuid) {
    return facturapi.obtenerZip(obtenerFacturapiId(uuid));
  }

  private String obtenerFacturapiId(String uuid) {
    Factura factura = facturaService.obtenerPorUuid(uuid);
    if (factura.getFacturapiId() == null || factura.getFacturapiId().isBlank()) {
      throw new FacturaNoEncontradaException("La factura no tiene comprobante timbrado asociado");
    }
    return factura.getFacturapiId();
  }

  private Factura timbrarYRegistrar(Ticket ticket, Cliente cliente) {
    String noTicket = validarTicketParaFacturar(ticket, "facturar");
    if (facturaService.existePorNoTicket(noTicket)) {
      throw new FacturaDuplicadaException("La factura ya ha sido generada para el ticket " + noTicket);
    }
    FacturaTimbrada timbrada = timbrar(ticket, cliente);
    return crearFactura(ticket, cliente, noTicket, timbrada);
  }

  private Factura timbrarYRefacturar(Ticket ticket, Cliente cliente) {
    String noTicket = validarTicketParaFacturar(ticket, "refacturar");
    FacturaTimbrada timbrada = timbrar(ticket, cliente);
    cancelarFacturaPrevia(noTicket, timbrada.getId());
    return crearFactura(ticket, cliente, noTicket, timbrada);
  }

  private String validarTicketParaFacturar(Ticket ticket, String operacion) {
    if (ticket == null || ticket.getNumeroTicket() == null || ticket.getNumeroTicket().isBlank()) {
      throw new IllegalArgumentException("El numero de ticket es obligatorio para " + operacion);
    }
    return ticket.getNumeroTicket().trim();
  }

  private String validarRfc(String rfc, String operacion) {
    if (rfc == null || rfc.isBlank()) {
      throw new IllegalArgumentException("El RFC del cliente es obligatorio para " + operacion);
    }
    String normalizado = rfc.trim().toUpperCase(Locale.ROOT);
    if (!RFC_PATTERN.matcher(normalizado).matches()) {
      throw new IllegalArgumentException(
          "El RFC '" + normalizado + "' no tiene un formato valido del SAT (12 caracteres para "
              + "persona moral o 13 para persona fisica). Ejemplos: ABC010101AB1 (moral), "
              + "ABCD010101AB1 (fisica), XAXX010101000 (publico en general)");
    }
    return normalizado;
  }

  private FacturaTimbrada timbrar(Ticket ticket, Cliente cliente) {
    FacturaTimbrada timbrada = facturapi.timbrar(construirSolicitud(ticket, cliente));
    if (timbrada == null || timbrada.getUuid() == null || timbrada.getUuid().isBlank()) {
      throw new TimbradoFallidoException("Facturapi no regreso un UUID valido del CFDI");
    }
    return timbrada;
  }

  private SolicitudTimbrado construirSolicitud(Ticket ticket, Cliente cliente) {
    SolicitudTimbrado solicitud = new SolicitudTimbrado();
    solicitud.setRfc(cliente.getRfc());
    solicitud.setRazonSocial(cliente.getRazonSocial());
    solicitud.setRegimenFiscal(cliente.getRegimenFiscal());
    solicitud.setCodigoPostal(cliente.getCodigoPostal());
    solicitud.setCorreo(cliente.getCorreoElectronico());
    solicitud.setUsoCfdi(cliente.getUsoFactura());
    solicitud.setFormaPago(resolverFormaPago(ticket.getPagos()));
    solicitud.setMetodoPago(propiedades.getPaymentMethod());
    solicitud.setPrecioConImpuestos(propiedades.isTaxIncluded());
    solicitud.setFolioExterno(ticket.getNumeroTicket());
    aplicarReglasPublicoGeneral(solicitud, ticket);
    List<ItemSolicitudTimbrado> items = new ArrayList<>();
    if (ticket.getItems() != null) {
      for (var item : ticket.getItems()) {
        BigDecimal cantidad = item.getCantidad() != null ? item.getCantidad() : BigDecimal.ONE;
        BigDecimal precio = item.getPrecioUnitario();
        if (precio == null && item.getImporte() != null && cantidad.compareTo(BigDecimal.ZERO) > 0) {
          precio = item.getImporte().divide(cantidad, 2, java.math.RoundingMode.HALF_UP);
        }
        String descripcion = item.getDescripcion() != null && !item.getDescripcion().isBlank()
            ? item.getDescripcion() : "Item " + item.getId();
        items.add(new ItemSolicitudTimbrado(cantidad, descripcion, precio,
            propiedades.getProductKey()));
      }
    }
    solicitud.setItems(items);
    return solicitud;
  }

  private static final String RFC_PUBLICO_GENERAL = "XAXX010101000";

  private void aplicarReglasPublicoGeneral(SolicitudTimbrado solicitud, Ticket ticket) {
    if (!RFC_PUBLICO_GENERAL.equalsIgnoreCase(solicitud.getRfc())) {
      return;
    }
    solicitud.setPublicoGeneral(true);
    solicitud.setRazonSocial("PUBLICO EN GENERAL");
    solicitud.setRegimenFiscal("616");
    solicitud.setUsoCfdi("S01");
    String periodicidad = propiedades.getPublicoGeneralPeriodicity();
    solicitud.setPeriodoPeriodicity(periodicidad);
    LocalDate fecha = fechaOperacion(ticket);
    solicitud.setPeriodoMeses(resolverMesGlobal(fecha, periodicidad));
    solicitud.setPeriodoAnio(fecha.getYear());
  }

  private LocalDate fechaOperacion(Ticket ticket) {
    Instant referencia = ticket.getFechaCierre() != null ? ticket.getFechaCierre()
        : ticket.getFechaEmision() != null ? ticket.getFechaEmision() : Instant.now();
    return referencia.atZone(ZoneId.of("America/Mexico_City")).toLocalDate();
  }

  private String resolverMesGlobal(LocalDate fecha, String periodicidad) {
    if ("two_months".equalsIgnoreCase(periodicidad)) {
      int bimester = (fecha.getMonthValue() + 1) / 2 + 12;
      return String.format("%02d", bimester);
    }
    return String.format("%02d", fecha.getMonthValue());
  }

  private String resolverFormaPago(List<TicketPago> pagos) {
    if (pagos == null || pagos.isEmpty()) {
      return propiedades.getPaymentForm();
    }
    String metodo = pagos.get(0).getMetodo() == null
        ? "" : pagos.get(0).getMetodo().toLowerCase(Locale.ROOT);
    if (metodo.contains("efectivo") || metodo.contains("cash")) {
      return "01";
    }
    if (metodo.contains("transfer") || metodo.contains("spei")) {
      return "03";
    }
    return propiedades.getPaymentForm();
  }

  private void aplicarMontosTimbrados(Ticket ticket, Factura factura) {
    if (ticket == null || factura == null) {
      return;
    }
    ticket.setSubtotal(factura.getSubtotal());
    ticket.setImpuestos(factura.getImpuesto());
    ticket.setTotal(factura.getTotal());
  }

  private void cancelarFacturaPrevia(String noTicket, String sustitucion) {
    try {
      Factura previa = facturaService.obtenerPorNoTicket(noTicket);
      if (previa == null || "CANCELADA".equalsIgnoreCase(previa.getEstatus())) {
        return;
      }
      if (previa.getFacturapiId() == null || previa.getFacturapiId().isBlank()) {
        facturaService.eliminarPorId(previa.getIdFactura());
        return;
      }
      try {
        ResultadoCancelacion resultado =
            facturapi.cancelar(previa.getFacturapiId(), "01", sustitucion);
        previa.setCancellationStatus(resultado.getCancellationStatus());
        if (resultado.aceptada()) {
          previa.setEstatus("CANCELADA");
        }
        facturaService.actualizarPorId(previa.getIdFactura(), previa);
      } catch (RuntimeException ex) {
        log.warn("No fue posible cancelar en Facturapi la factura previa uuid={}: {}",
            previa.getUuid(), ex.getMessage());
        previa.setCancellationStatus("error");
        facturaService.actualizarPorId(previa.getIdFactura(), previa);
      }
    } catch (FacturaNoEncontradaException ignored) {
    }
  }

  private Factura crearFactura(Ticket ticket, Cliente cliente, String noTicket,
                               FacturaTimbrada timbrada) {
    Factura factura = new Factura();
    factura.setFolio(timbrada.getFolio() != null ? timbrada.getFolio() : noTicket);
    factura.setSerie(timbrada.getSerie() != null ? timbrada.getSerie() : "");
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
    factura.setSubtotal(timbrada.getSubtotal() != null ? timbrada.getSubtotal() : ticket.getSubtotal());
    factura.setImpuesto(timbrada.getImpuesto() != null ? timbrada.getImpuesto() : ticket.getImpuestos());
    factura.setTotal(timbrada.getTotal() != null ? timbrada.getTotal() : ticket.getTotal());
    factura.setEstatus("Cargada");
    factura.setUuid(timbrada.getUuid());
    factura.setFacturapiId(timbrada.getId());
    factura.setCancellationStatus(timbrada.getCancellationStatus());
    return facturaService.crear(factura);
  }
}
