package com.portalfacturacion.application.service;

import com.portalfacturacion.application.port.in.factura.EnviarFacturaUseCase;
import com.portalfacturacion.application.port.out.persistence.FacturaRepositoryPort;
import com.portalfacturacion.application.port.out.notification.CorreoPort;
import com.portalfacturacion.domain.exception.FacturaNoEncontradaException;
import com.portalfacturacion.domain.model.Factura;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EnvioFacturaService implements EnviarFacturaUseCase {

  private static final Logger log = LoggerFactory.getLogger(EnvioFacturaService.class);

  private final FacturaRepositoryPort facturaRepo;
  private final PlantillaCorreoService plantillas;
  private final CorreoPort correo;

  public EnvioFacturaService(FacturaRepositoryPort facturaRepo,
                                PlantillaCorreoService plantillas,
                                CorreoPort correo) {
    this.facturaRepo = facturaRepo;
    this.plantillas = plantillas;
    this.correo = correo;
  }

  @Override
  public CorreoPreparado enviarFactura(String correoElectronico, String numeroTicket) {
    Factura factura = facturaRepo.findByNoTicket(numeroTicket)
        .orElseThrow(() -> new FacturaNoEncontradaException(
          "No se encontro ninguna factura asociada al ticket: " + numeroTicket));
    CorreoPreparado preparado = plantillas.construirCorreo("correo_factura.html",
      "titulo=" + "Facturación Oso Despierto",
      "folio=" + factura.getFolio(),
      "total=" + (factura.getTotal() != null ? factura.getTotal().toPlainString() : "0.00"),
      "cliente=" + (factura.getCliente() != null ? factura.getCliente() : "Sin nombre"));
    correo.enviarTextoPlano(correoElectronico, preparado.asunto(), preparado.contenido());
    log.info("Correo enviado a: " + correoElectronico + " con el asunto: " + preparado.asunto());
    return preparado;
  }

  @Override
  public CorreoPreparado reenviarFactura(String correoElectronico, String numeroTicket) {
    Factura factura = facturaRepo.findByNoTicket(numeroTicket)
        .orElseThrow(() -> new FacturaNoEncontradaException(
          "No se encontro ninguna factura asociada al ticket: " + numeroTicket));
    CorreoPreparado preparado = plantillas.construirCorreo("correo_factura.html",
      "titulo=" + "Facturación Oso Despierto",
      "folio=" + factura.getFolio(),
      "total=" + (factura.getTotal() != null ? factura.getTotal().toPlainString() : "0.00"),
      "cliente=" + (factura.getCliente() != null ? factura.getCliente() : "Sin nombre"));
    correo.enviarTextoPlano(correoElectronico, preparado.asunto(), preparado.contenido());
    log.info("Correo reenviado a: " + correoElectronico + " con el asunto: " + preparado.asunto());
    return preparado;
  }
}
