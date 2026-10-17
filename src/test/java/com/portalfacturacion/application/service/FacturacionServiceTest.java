package com.portalfacturacion.application.service;

import com.portalfacturacion.application.port.out.facturapi.FacturapiTimbradoPort;
import com.portalfacturacion.application.port.out.notification.CorreoPort;
import com.portalfacturacion.configuration.FacturapiProperties;
import com.portalfacturacion.domain.exception.FacturaDuplicadaException;
import com.portalfacturacion.domain.exception.TimbradoFallidoException;
import com.portalfacturacion.domain.model.Cliente;
import com.portalfacturacion.domain.model.DatosFactura;
import com.portalfacturacion.domain.model.Factura;
import com.portalfacturacion.domain.model.FacturaTimbrada;
import com.portalfacturacion.domain.model.ResultadoCancelacion;
import com.portalfacturacion.domain.model.SolicitudTimbrado;
import com.portalfacturacion.domain.model.Ticket;
import com.portalfacturacion.domain.model.TicketItem;
import com.portalfacturacion.domain.model.TicketPago;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FacturacionServiceTest {

  private TicketService ticketService;
  private ClienteService clienteService;
  private FacturaService facturaService;
  private PlantillaCorreoService plantillas;
  private CorreoPort correo;
  private FacturapiTimbradoPort facturapi;
  private FacturacionService service;

  @BeforeEach
  void setUp() {
    ticketService = mock(TicketService.class);
    clienteService = mock(ClienteService.class);
    facturaService = mock(FacturaService.class);
    plantillas = mock(PlantillaCorreoService.class);
    correo = mock(CorreoPort.class);
    facturapi = mock(FacturapiTimbradoPort.class);
    service = new FacturacionService(ticketService, clienteService, facturaService,
        plantillas, correo, facturapi, new FacturapiProperties());
    when(plantillas.construirCorreo(anyString(), any(String[].class)))
        .thenReturn(new CorreoPreparado("remitente", "Oso", "asunto", "<html></html>"));
  }

  private Ticket ticket() {
    return new Ticket("T-1", new BigDecimal("1160.00"), new BigDecimal("1160.00"), null,
        "CLOSED", "EAT-IN", Instant.now(), Instant.now(), "Cliente Ticket", "Mesa 1", null, true,
        List.of(new TicketItem("10", "Cafe", BigDecimal.ONE, new BigDecimal("1160.00"),
            new BigDecimal("1160.00"))),
        List.of(new TicketPago("20", "Cash", new BigDecimal("1160.00"))));
  }

  private Cliente cliente() {
    return new Cliente(1L, "AAAA111111AAA", "Empresa X SA", "Calle 1", "10", null, null,
        "CDMX", "Cuauhtemoc", "Centro", "06000", "cliente@correo.com", "601", "G03",
        true, null, null);
  }

  private FacturaTimbrada timbrada(String id, String uuid) {
    FacturaTimbrada t = new FacturaTimbrada();
    t.setId(id);
    t.setUuid(uuid);
    t.setSerie("A");
    t.setFolio("100");
    t.setStatus("valid");
    t.setCancellationStatus("none");
    t.setSubtotal(new BigDecimal("1000.00"));
    t.setImpuesto(new BigDecimal("160.00"));
    t.setTotal(new BigDecimal("1160.00"));
    return t;
  }

  private ResultadoCancelacion cancelacionAceptada() {
    ResultadoCancelacion r = new ResultadoCancelacion();
    r.setId("fp1");
    r.setUuid("uuid-0");
    r.setStatus("canceled");
    r.setCancellationStatus("accepted");
    return r;
  }

  private void stubGuardarCliente(Cliente c) {
    when(clienteService.existePorRfc(c.getRfc())).thenReturn(false);
    when(clienteService.crear(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  private void stubCrearFactura() {
    when(facturaService.crear(any(Factura.class))).thenAnswer(inv -> {
      Factura f = inv.getArgument(0);
      f.setIdFactura(1L);
      return f;
    });
  }

  @Test
  void facturar_timbraEnFacturapi_yGuardaUuidReal() {
    Cliente c = cliente();
    stubGuardarCliente(c);
    when(facturaService.existePorNoTicket("T-1")).thenReturn(false);
    when(facturapi.timbrar(any(SolicitudTimbrado.class))).thenReturn(timbrada("fp1", "uuid-1"));
    stubCrearFactura();
    when(facturapi.obtenerZip("fp1")).thenReturn("zip".getBytes());
    DatosFactura datos = new DatosFactura(ticket(), c);

    DatosFactura resultado = service.facturar(datos);

    ArgumentCaptor<SolicitudTimbrado> solicitud = ArgumentCaptor.forClass(SolicitudTimbrado.class);
    verify(facturapi).timbrar(solicitud.capture());
    assertThat(solicitud.getValue().getRfc()).isEqualTo("AAAA111111AAA");
    assertThat(solicitud.getValue().getUsoCfdi()).isEqualTo("G03");
    assertThat(solicitud.getValue().getFormaPago()).isEqualTo("01");
    assertThat(solicitud.getValue().getMetodoPago()).isEqualTo("PUE");
    assertThat(solicitud.getValue().isPrecioConImpuestos()).isTrue();
    assertThat(solicitud.getValue().getItems()).hasSize(1);
    assertThat(solicitud.getValue().getItems().get(0).getPrecio()).isEqualByComparingTo("1160.00");

    ArgumentCaptor<Factura> creada = ArgumentCaptor.forClass(Factura.class);
    verify(facturaService).crear(creada.capture());
    Factura f = creada.getValue();
    assertThat(f.getUuid()).isEqualTo("uuid-1");
    assertThat(f.getFacturapiId()).isEqualTo("fp1");
    assertThat(f.getSerie()).isEqualTo("A");
    assertThat(f.getFolio()).isEqualTo("100");
    assertThat(f.getEstatus()).isEqualTo("Cargada");
    assertThat(f.getSubtotal()).isEqualByComparingTo("1000.00");
    assertThat(f.getImpuesto()).isEqualByComparingTo("160.00");
    assertThat(f.getTotal()).isEqualByComparingTo("1160.00");

    assertThat(resultado.getTicket().getTotal()).isEqualByComparingTo("1160.00");
    verify(correo).enviarTextoPlanoConAdjuntos(eq("cliente@correo.com"), anyString(),
        anyString(), anyList());
  }

  @Test
  void facturar_ticketYaFacturado_lanzaDuplicada() {
    when(facturaService.existePorNoTicket("T-1")).thenReturn(true);
    DatosFactura datos = new DatosFactura(ticket(), cliente());

    assertThatThrownBy(() -> service.facturar(datos))
        .isInstanceOf(FacturaDuplicadaException.class);
    verify(facturapi, never()).timbrar(any());
  }

  @Test
  void facturar_respuestaSinUuid_lanzaTimbradoFallido() {
    Cliente c = cliente();
    stubGuardarCliente(c);
    when(facturaService.existePorNoTicket("T-1")).thenReturn(false);
    when(facturapi.timbrar(any(SolicitudTimbrado.class))).thenReturn(timbrada("fp1", null));
    DatosFactura datos = new DatosFactura(ticket(), c);

    assertThatThrownBy(() -> service.facturar(datos))
        .isInstanceOf(TimbradoFallidoException.class);
    verify(facturaService, never()).crear(any(Factura.class));
  }

  @Test
  void facturar_rfcInvalido_lanzaErrorAntesDeTimbrar() {
    Cliente c = cliente();
    c.setRfc("RFC-INVALIDO");
    DatosFactura datos = new DatosFactura(ticket(), c);

    assertThatThrownBy(() -> service.facturar(datos))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("formato valido del SAT");
    verify(facturapi, never()).timbrar(any());
    verify(facturaService, never()).crear(any(Factura.class));
  }

  @Test
  void facturar_rfcVacio_lanzaErrorAntesDeTimbrar() {
    Cliente c = cliente();
    c.setRfc("   ");
    DatosFactura datos = new DatosFactura(ticket(), c);

    assertThatThrownBy(() -> service.facturar(datos))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("obligatorio");
    verify(facturapi, never()).timbrar(any());
  }

  @Test
  void facturar_publicoGeneral_fuerzaRegimenUsoYPeriodoGlobal() {
    Cliente c = cliente();
    c.setRfc("XAXX010101000");
    c.setRazonSocial("Juan Perez");
    c.setRegimenFiscal("601");
    c.setUsoFactura("G03");
    stubGuardarCliente(c);
    when(facturaService.existePorNoTicket("T-1")).thenReturn(false);
    when(facturapi.timbrar(any(SolicitudTimbrado.class))).thenReturn(timbrada("fp1", "uuid-1"));
    stubCrearFactura();
    when(facturapi.obtenerZip("fp1")).thenReturn("zip".getBytes());
    DatosFactura datos = new DatosFactura(ticket(), c);

    service.facturar(datos);

    ArgumentCaptor<SolicitudTimbrado> solicitud = ArgumentCaptor.forClass(SolicitudTimbrado.class);
    verify(facturapi).timbrar(solicitud.capture());
    SolicitudTimbrado s = solicitud.getValue();
    assertThat(s.isPublicoGeneral()).isTrue();
    assertThat(s.getRazonSocial()).isEqualTo("PUBLICO EN GENERAL");
    assertThat(s.getRegimenFiscal()).isEqualTo("616");
    assertThat(s.getUsoCfdi()).isEqualTo("S01");
    assertThat(s.getPeriodoPeriodicity()).isEqualTo("day");
    java.time.LocalDate hoy = java.time.LocalDate.now(java.time.ZoneId.of("America/Mexico_City"));
    assertThat(s.getPeriodoMeses()).isEqualTo(String.format("%02d", hoy.getMonthValue()));
    assertThat(s.getPeriodoAnio()).isEqualTo(hoy.getYear());
  }

  @Test
  void facturar_rfcNormal_noActivaPublicoGeneral() {
    Cliente c = cliente();
    stubGuardarCliente(c);
    when(facturaService.existePorNoTicket("T-1")).thenReturn(false);
    when(facturapi.timbrar(any(SolicitudTimbrado.class))).thenReturn(timbrada("fp1", "uuid-1"));
    stubCrearFactura();
    when(facturapi.obtenerZip("fp1")).thenReturn("zip".getBytes());
    DatosFactura datos = new DatosFactura(ticket(), c);

    service.facturar(datos);

    ArgumentCaptor<SolicitudTimbrado> solicitud = ArgumentCaptor.forClass(SolicitudTimbrado.class);
    verify(facturapi).timbrar(solicitud.capture());
    assertThat(solicitud.getValue().isPublicoGeneral()).isFalse();
    assertThat(solicitud.getValue().getRegimenFiscal()).isEqualTo("601");
    assertThat(solicitud.getValue().getUsoCfdi()).isEqualTo("G03");
  }

  @Test
  void refacturar_cancelaPreviaConMotivo01YSustitucion() {
    Cliente c = cliente();
    when(clienteService.obtenerPorRfc("AAAA111111AAA")).thenReturn(c);
    when(facturapi.timbrar(any(SolicitudTimbrado.class))).thenReturn(timbrada("fp2", "uuid-2"));
    Factura previa = new Factura();
    previa.setIdFactura(9L);
    previa.setFolio("99");
    previa.setSerie("A");
    previa.setRfc("AAAA111111AAA");
    previa.setRazonSocial("Empresa X SA");
    previa.setNoTicket("T-1");
    previa.setUuid("uuid-0");
    previa.setFacturapiId("fp1");
    previa.setEstatus("Cargada");
    when(facturaService.obtenerPorNoTicket("T-1")).thenReturn(previa);
    when(facturapi.cancelar("fp1", "01", "fp2")).thenReturn(cancelacionAceptada());
    when(facturaService.actualizarPorId(any(), any(Factura.class)))
        .thenAnswer(inv -> inv.getArgument(1));
    stubCrearFactura();
    when(facturapi.obtenerZip("fp2")).thenReturn("zip".getBytes());
    DatosFactura datos = new DatosFactura(ticket(), c);

    service.refacturar(datos);

    verify(facturapi).cancelar(eq("fp1"), eq("01"), eq("fp2"));
    assertThat(previa.getEstatus()).isEqualTo("CANCELADA");
    assertThat(previa.getCancellationStatus()).isEqualTo("accepted");
    ArgumentCaptor<Factura> creada = ArgumentCaptor.forClass(Factura.class);
    verify(facturaService).crear(creada.capture());
    assertThat(creada.getValue().getUuid()).isEqualTo("uuid-2");
    assertThat(creada.getValue().getFacturapiId()).isEqualTo("fp2");
  }

  @Test
  void refacturar_previaSinTimbrado_soloCancelaLocal() {
    Cliente c = cliente();
    when(clienteService.obtenerPorRfc("AAAA111111AAA")).thenReturn(c);
    when(facturapi.timbrar(any(SolicitudTimbrado.class))).thenReturn(timbrada("fp2", "uuid-2"));
    Factura previa = new Factura();
    previa.setIdFactura(9L);
    previa.setFolio("T-1");
    previa.setRfc("AAAA111111AAA");
    previa.setRazonSocial("Empresa X SA");
    previa.setNoTicket("T-1");
    previa.setEstatus("Cargada");
    when(facturaService.obtenerPorNoTicket("T-1")).thenReturn(previa);
    stubCrearFactura();
    when(facturapi.obtenerZip("fp2")).thenReturn("zip".getBytes());
    DatosFactura datos = new DatosFactura(ticket(), c);

    service.refacturar(datos);

    verify(facturaService).eliminarPorId(9L);
    verify(facturapi, never()).cancelar(anyString(), anyString(), any());
  }

  @Test
  void cancelarFactura_aceptada_marcaCancelada() {
    Factura factura = new Factura();
    factura.setIdFactura(5L);
    factura.setUuid("uuid-1");
    factura.setFacturapiId("fp1");
    factura.setEstatus("Cargada");
    factura.setRfc("AAAA111111AAA");
    factura.setRazonSocial("Empresa X SA");
    factura.setNoTicket("T-1");
    when(facturaService.obtenerPorUuid("uuid-1")).thenReturn(factura);
    when(facturapi.cancelar("fp1", "02", null)).thenReturn(cancelacionAceptada());
    when(facturaService.actualizarPorId(any(), any(Factura.class)))
        .thenAnswer(inv -> inv.getArgument(1));

    Factura resultado = service.cancelarFactura("uuid-1", null);

    verify(facturapi).cancelar("fp1", "02", null);
    assertThat(resultado.getEstatus()).isEqualTo("CANCELADA");
    assertThat(resultado.getCancellationStatus()).isEqualTo("accepted");
  }

  @Test
  void cancelarFactura_motivoCero1_lanzaError() {
    Factura factura = new Factura();
    factura.setIdFactura(5L);
    factura.setUuid("uuid-1");
    factura.setFacturapiId("fp1");
    factura.setEstatus("Cargada");
    when(facturaService.obtenerPorUuid("uuid-1")).thenReturn(factura);

    assertThatThrownBy(() -> service.cancelarFactura("uuid-1", "01"))
        .isInstanceOf(IllegalArgumentException.class);
    verify(facturapi, never()).cancelar(anyString(), anyString(), any());
  }

  @Test
  void descargarXml_usaFacturapiIdDeLaFactura() {
    Factura factura = new Factura();
    factura.setUuid("uuid-1");
    factura.setFacturapiId("fp1");
    when(facturaService.obtenerPorUuid("uuid-1")).thenReturn(factura);
    when(facturapi.obtenerXml("fp1")).thenReturn("<xml/>".getBytes());

    byte[] xml = service.descargarXml("uuid-1");

    assertThat(new String(xml)).isEqualTo("<xml/>");
  }
}
