package com.portalfacturacion.application.service;

import com.portalfacturacion.application.port.out.facturapi.FacturapiTimbradoPort;
import com.portalfacturacion.application.port.out.notification.CorreoPort;
import com.portalfacturacion.application.port.out.persistence.FacturaRepositoryPort;
import com.portalfacturacion.domain.exception.FacturaNoEncontradaException;
import com.portalfacturacion.domain.model.Factura;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

class EnvioFacturaServiceTest {

  private FacturaRepositoryPort facturaRepo;
  private PlantillaCorreoService plantillas;
  private CorreoPort correo;
  private FacturapiTimbradoPort facturapi;
  private EnvioFacturaService service;

  @BeforeEach
  void setUp() {
    facturaRepo = mock(FacturaRepositoryPort.class);
    plantillas = mock(PlantillaCorreoService.class);
    correo = mock(CorreoPort.class);
    facturapi = mock(FacturapiTimbradoPort.class);
    service = new EnvioFacturaService(facturaRepo, plantillas, correo, facturapi);
    when(plantillas.construirCorreo(anyString(), any(String[].class)))
        .thenReturn(new CorreoPreparado("remitente", "Oso", "asunto", "<html></html>"));
  }

  private Factura facturaConTimbrado() {
    Factura f = new Factura();
    f.setIdFactura(1L);
    f.setFolio("100");
    f.setNoTicket("T-1");
    f.setUuid("uuid-1");
    f.setFacturapiId("fp1");
    f.setTotal(new BigDecimal("1160.00"));
    f.setCliente("Empresa X SA");
    return f;
  }

  @Test
  void enviarFactura_conFacturapiId_adjuntaXmlYPdf() {
    Factura factura = facturaConTimbrado();
    when(facturaRepo.findByNoTicket("T-1")).thenReturn(Optional.of(factura));
    when(facturapi.obtenerXml("fp1")).thenReturn("<xml/>".getBytes());
    when(facturapi.obtenerPdf("fp1")).thenReturn("pdf".getBytes());

    service.enviarFactura("cliente@correo.com", "T-1");

    ArgumentCaptor<List<File>> adjuntos = ArgumentCaptor.forClass(List.class);
    verify(correo).enviarTextoPlanoConAdjuntos(eq("cliente@correo.com"), anyString(),
        anyString(), adjuntos.capture());
    List<File> capturados = adjuntos.getValue();
    assertThat(capturados).hasSize(2);
    assertThat(capturados.get(0).getName()).endsWith(".xml");
    assertThat(capturados.get(1).getName()).endsWith(".pdf");
    verify(correo, never()).enviarTextoPlano(anyString(), anyString(), anyString());
    capturados.forEach(File::delete);
  }

  @Test
  void enviarFactura_sinFacturapiId_enviaSoloTexto() {
    Factura factura = facturaConTimbrado();
    factura.setFacturapiId(null);
    when(facturaRepo.findByNoTicket("T-1")).thenReturn(Optional.of(factura));

    service.enviarFactura("cliente@correo.com", "T-1");

    verify(correo).enviarTextoPlano("cliente@correo.com", "asunto", "<html></html>");
    verify(correo, never()).enviarTextoPlanoConAdjuntos(anyString(), anyString(),
        anyString(), anyList());
  }

  @Test
  void reenviarFactura_conFacturapiId_adjuntaXmlYPdf() {
    Factura factura = facturaConTimbrado();
    when(facturaRepo.findByNoTicket("T-1")).thenReturn(Optional.of(factura));
    when(facturapi.obtenerXml("fp1")).thenReturn("<xml/>".getBytes());
    when(facturapi.obtenerPdf("fp1")).thenReturn("pdf".getBytes());

    service.reenviarFactura("cliente@correo.com", "T-1");

    verify(facturapi).obtenerXml("fp1");
    verify(facturapi).obtenerPdf("fp1");
    verify(correo).enviarTextoPlanoConAdjuntos(eq("cliente@correo.com"), anyString(),
        anyString(), anyList());
  }

  @Test
  void enviarFactura_noEncontrada_lanzaExcepcion() {
    when(facturaRepo.findByNoTicket("T-1")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.enviarFactura("cliente@correo.com", "T-1"))
        .isInstanceOf(FacturaNoEncontradaException.class);
    verify(correo, never()).enviarTextoPlano(anyString(), anyString(), anyString());
  }
}
