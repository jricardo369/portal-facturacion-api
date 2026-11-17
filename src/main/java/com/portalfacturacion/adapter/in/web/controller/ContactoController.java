package com.portalfacturacion.adapter.in.web.controller;

import com.portalfacturacion.adapter.in.web.request.SolicitarSoporteRequest;
import com.portalfacturacion.adapter.in.web.response.SolicitudSoporteResponse;
import com.portalfacturacion.application.port.in.contacto.SolicitudSoporteCommand;
import com.portalfacturacion.application.service.CorreoContactoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contacto")
@Tag(name = "Contacto", description = "Envio de dudas y aclaraciones de facturas al correo configurado")
public class ContactoController {

  private static final Logger log = LoggerFactory.getLogger(ContactoController.class);

  private final CorreoContactoService service;

  public ContactoController(CorreoContactoService service) {
    this.service = service;
  }

  @PostMapping
  @Operation(summary = "Enviar duda o aclaracion de factura",
      description = "Motivo DUDA: se envia nombre y descripcion. Motivo ACLARACION: se envia nombre, "
          + "fecha, hora, total y descripcion. El correo destino se toma de la tabla de configuracion "
          + "(clave correo_dudas_aclaraciones, por defecto osodespiertocoffee@gmail.com).")
  public ResponseEntity<SolicitudSoporteResponse> enviar(
      @Valid @RequestBody SolicitarSoporteRequest request) {
    SolicitudSoporteCommand command = new SolicitudSoporteCommand(
        request.nombre(),
        SolicitudSoporteCommand.Motivo.desde(request.motivo()),
        request.fecha(),
        request.hora(),
        request.total(),
        request.descripcion());
    service.enviar(command);
    return ResponseEntity.ok(SolicitudSoporteResponse.of(service.resolverDestinatario()));
  }
}
