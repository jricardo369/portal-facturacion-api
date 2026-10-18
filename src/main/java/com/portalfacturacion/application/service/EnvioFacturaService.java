package com.portalfacturacion.application.service;

import com.portalfacturacion.application.port.in.factura.EnviarFacturaUseCase;
import com.portalfacturacion.application.port.out.facturapi.FacturapiTimbradoPort;
import com.portalfacturacion.application.port.out.persistence.FacturaRepositoryPort;
import com.portalfacturacion.application.port.out.notification.CorreoPort;
import com.portalfacturacion.domain.exception.FacturaNoEncontradaException;
import com.portalfacturacion.domain.model.Factura;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EnvioFacturaService implements EnviarFacturaUseCase {

  private static final Logger log = LoggerFactory.getLogger(EnvioFacturaService.class);

  private final FacturaRepositoryPort facturaRepo;
  private final PlantillaCorreoService plantillas;
  private final CorreoPort correo;
  private final FacturapiTimbradoPort facturapi;

  public EnvioFacturaService(FacturaRepositoryPort facturaRepo,
                                PlantillaCorreoService plantillas,
                                CorreoPort correo,
                                FacturapiTimbradoPort facturapi) {
    this.facturaRepo = facturaRepo;
    this.plantillas = plantillas;
    this.correo = correo;
    this.facturapi = facturapi;
  }

  @Override
  public CorreoPreparado enviarFactura(String correoElectronico, String numeroTicket) {
    Factura factura = facturaRepo.findByNoTicket(numeroTicket)
        .orElseThrow(() -> new FacturaNoEncontradaException(
          "No se encontro ninguna factura asociada al ticket: " + numeroTicket));
    CorreoPreparado preparado = construirCorreo(factura);
    enviar(correoElectronico, preparado, factura);
    log.info("Correo enviado a: " + correoElectronico + " con el asunto: " + preparado.asunto());
    return preparado;
  }

  @Override
  public CorreoPreparado reenviarFactura(String correoElectronico, String numeroTicket) {
    Factura factura = facturaRepo.findByNoTicket(numeroTicket)
        .orElseThrow(() -> {
          log.warn("No se reenvia correo: la factura con el folio proporcionado no fue encontrada: {}", numeroTicket);
          return new FacturaNoEncontradaException(
            "La factura con el folio proporcionado no fue encontrada");
        });
    // El correo solo se envia si la factura fue encontrada.
    CorreoPreparado preparado = construirCorreo(factura);
    enviar(correoElectronico, preparado, factura);
    log.info("Correo reenviado a: " + correoElectronico + " con el asunto: " + preparado.asunto());
    return preparado;
  }

  private CorreoPreparado construirCorreo(Factura factura) {
    return plantillas.construirCorreo("correo_factura.html",
      "titulo=" + "Facturación Oso Despierto",
      "folio=" + factura.getFolio(),
      "total=" + (factura.getTotal() != null ? factura.getTotal().toPlainString() : "0.00"),
      "cliente=" + (factura.getCliente() != null ? factura.getCliente() : "Sin nombre"));
  }

  private void enviar(String correoElectronico, CorreoPreparado preparado, Factura factura) {
    List<File> adjuntos = adjuntarXmlYPdf(factura);
    if (adjuntos.isEmpty()) {
      correo.enviarTextoPlano(correoElectronico, preparado.asunto(), preparado.contenido());
      return;
    }
    correo.enviarTextoPlanoConAdjuntos(correoElectronico, preparado.asunto(),
        preparado.contenido(), adjuntos);
  }

  private List<File> adjuntarXmlYPdf(Factura factura) {
    List<File> adjuntos = new ArrayList<>();
    if (factura.getFacturapiId() == null || factura.getFacturapiId().isBlank()) {
      log.warn("La factura {} no tiene comprobante timbrado en Facturapi; se omite el envio de XML y PDF",
          factura.getUuid());
      return adjuntos;
    }
    String base = factura.getUuid() != null && !factura.getUuid().isBlank()
        ? factura.getUuid() : factura.getNoTicket();
    agregarAdjunto(adjuntos, base + ".xml",
        () -> facturapi.obtenerXml(factura.getFacturapiId()));
    agregarAdjunto(adjuntos, base + ".pdf",
        () -> facturapi.obtenerPdf(factura.getFacturapiId()));
    return adjuntos;
  }

  private void agregarAdjunto(List<File> adjuntos, String nombre, java.util.function.Supplier<byte[]> contenido) {
    try {
      byte[] bytes = contenido.get();
      if (bytes == null || bytes.length == 0) {
        return;
      }
      File archivo = File.createTempFile("cfdi-", "-" + nombre);
      archivo.deleteOnExit();
      Files.write(archivo.toPath(), bytes);
      adjuntos.add(archivo);
    } catch (IOException | RuntimeException ex) {
      log.warn("No fue posible preparar el adjunto {}: {}", nombre, ex.getMessage());
    }
  }
}
